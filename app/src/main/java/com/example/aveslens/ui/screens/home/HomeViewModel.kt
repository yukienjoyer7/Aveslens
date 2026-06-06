package com.example.aveslens.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.model.Observation
import com.example.aveslens.data.model.Profile
import com.example.aveslens.data.repository.ObservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ObservationRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _displayName = MutableStateFlow("Birder")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _greeting = MutableStateFlow("Good morning")
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    private var allObservations: List<Observation> = emptyList()

    init {
        updateGreeting()
        loadObservations()
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .collectLatest { query -> applyFilter(query) }
        }
    }

    fun loadObservations() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            fetchData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchData()
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private suspend fun fetchData() {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return
            // Load first name for greeting (best-effort, non-blocking)
            runCatching {
                val profile = supabase.from("user")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<Profile>()
                val firstName = profile?.fullName?.split(" ")?.firstOrNull()
                if (!firstName.isNullOrBlank()) _displayName.value = firstName
            }
            allObservations = repository.getObservations(userId)
            applyFilter(_searchQuery.value)
        } catch (e: Exception) {
            _uiState.value = HomeUiState.Error(e.message ?: "Failed to load observations")
        }
    }

    private fun applyFilter(query: String) {
        if (allObservations.isEmpty()) {
            _uiState.value = HomeUiState.Empty
            return
        }
        val filtered = if (query.isBlank()) {
            allObservations
        } else {
            allObservations.filter { obs ->
                val name = obs.speciesName ?: ""
                name.contains(query, ignoreCase = true) ||
                    obs.location?.contains(query, ignoreCase = true) == true
            }
        }
        _uiState.value = HomeUiState.Success(filtered)
    }

    private fun updateGreeting() {
        _greeting.value = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}

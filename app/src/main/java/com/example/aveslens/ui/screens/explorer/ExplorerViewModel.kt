package com.example.aveslens.ui.screens.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.repository.ObservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val repository: ObservationRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExplorerUiState>(ExplorerUiState.Loading)
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allSpecies: List<ObservedSpecies> = emptyList()

    init {
        loadSpecies()
        viewModelScope.launch {
            _searchQuery.debounce(300).collectLatest { query ->
                applyFilter(query)
            }
        }
    }

    fun loadSpecies() {
        viewModelScope.launch {
            _uiState.value = ExplorerUiState.Loading
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = ExplorerUiState.Error("Not logged in")
                    return@launch
                }
                val observations = repository.getObservations(userId)
                allSpecies = observations
                    .mapNotNull { it.speciesName?.trim() }
                    .filter { it.isNotBlank() }
                    .groupBy { it }
                    .map { (name, list) -> ObservedSpecies(name, list.size) }
                    .sortedBy { it.name }

                applyFilter(_searchQuery.value)
            } catch (e: Exception) {
                _uiState.value = ExplorerUiState.Error(e.message ?: "Failed to load species")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) allSpecies
        else allSpecies.filter { it.name.contains(query, ignoreCase = true) }

        _uiState.value = when {
            allSpecies.isEmpty() -> ExplorerUiState.Empty
            filtered.isEmpty() -> ExplorerUiState.Success(emptyList())
            else -> ExplorerUiState.Success(filtered)
        }
    }
}

package com.example.aveslens.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.model.Observation
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val observationId: String = checkNotNull(savedStateHandle["observationId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    init {
        loadObservation()
    }

    fun loadObservation() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val obs = supabase.from("bird_observation")
                    .select { filter { eq("id", observationId) } }
                    .decodeSingleOrNull<Observation>()
                _uiState.value = if (obs != null) DetailUiState.Success(obs) else DetailUiState.NotFound
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun deleteObservation() {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Deleting
            try {
                supabase.from("bird_observation")
                    .delete { filter { eq("id", observationId) } }
                _deleteState.value = DeleteState.Success
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "Failed to delete")
            }
        }
    }

    fun resetDeleteState() { _deleteState.value = DeleteState.Idle }
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Deleting : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

package com.example.aveslens.ui.screens.home

import com.example.aveslens.data.model.Observation

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val observations: List<Observation>) : HomeUiState()
    object Empty : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

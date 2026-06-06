package com.example.aveslens.ui.screens.detail

import com.example.aveslens.data.model.Observation

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val observation: Observation) : DetailUiState()
    object NotFound : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

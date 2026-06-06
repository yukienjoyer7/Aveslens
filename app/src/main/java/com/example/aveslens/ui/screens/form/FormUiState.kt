package com.example.aveslens.ui.screens.form

sealed class FormUiState {
    object Idle : FormUiState()
    object Saving : FormUiState()
    object Success : FormUiState()
    data class Error(val message: String) : FormUiState()
}

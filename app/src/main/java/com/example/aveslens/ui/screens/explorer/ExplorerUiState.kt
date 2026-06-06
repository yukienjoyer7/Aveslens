package com.example.aveslens.ui.screens.explorer

data class ObservedSpecies(val name: String, val count: Int)

sealed class ExplorerUiState {
    object Loading : ExplorerUiState()
    data class Success(val species: List<ObservedSpecies>) : ExplorerUiState()
    object Empty : ExplorerUiState()
    data class Error(val message: String) : ExplorerUiState()
}

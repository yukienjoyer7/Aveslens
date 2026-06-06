package com.example.aveslens.ui.screens.profile

import com.example.aveslens.data.model.Profile

data class ProfileStats(
    val birdsObserved: Int,
    val speciesIdentified: Int,
)

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: Profile, val stats: ProfileStats) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

package com.example.aveslens.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.model.Observation
import com.example.aveslens.data.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = ProfileUiState.Error("Not logged in")
                    return@launch
                }

                var profile = supabase.from("user")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<Profile>()

                if (profile == null) {
                    supabase.from("user").upsert(Profile(id = userId))
                    profile = Profile(id = userId)
                }

                val observations = supabase.from("bird_observation")
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Observation>()

                val stats = ProfileStats(
                    birdsObserved = observations.size,
                    speciesIdentified = observations.mapNotNull { it.speciesName }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .size,
                )

                _uiState.value = ProfileUiState.Success(profile, stats)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(fullName: String, username: String) {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                supabase.from("user").update({
                    set("full_name", fullName.trim())
                    set("username", username.trim())
                }) {
                    filter { eq("id", userId) }
                }
                load()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
            } catch (_: Exception) {
                // Sign out locally even if network fails
            }
            _logoutState.value = true
        }
    }
}

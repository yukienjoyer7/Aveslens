package com.example.aveslens.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (supabase.auth.currentSessionOrNull() != null) {
                _uiState.value = AuthUiState.Success
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(friendlyMessage(e))
            }
        }
    }

    fun register(email: String, password: String, fullName: String, username: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = AuthUiState.Error("Could not retrieve user after sign-up")
                    return@launch
                }
                supabase.from("user").upsert(
                    Profile(id = userId, fullName = fullName, username = username)
                )
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(friendlyMessage(e))
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun friendlyMessage(e: Exception): String {
        val msg = e.message ?: return "An error occurred"
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("Network is unreachable", ignoreCase = true) ||
            msg.contains("Failed to connect", ignoreCase = true) -> "No internet connection"
            msg.contains("Invalid login credentials", ignoreCase = true) -> "Wrong email or password"
            msg.contains("User already registered", ignoreCase = true) ||
            msg.contains("already been registered", ignoreCase = true) -> "Email already registered"
            msg.contains("duplicate key", ignoreCase = true) ||
            msg.contains("unique constraint", ignoreCase = true) -> "Username already taken"
            else -> msg
        }
    }
}

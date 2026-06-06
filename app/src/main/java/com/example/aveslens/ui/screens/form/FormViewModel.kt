package com.example.aveslens.ui.screens.form

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aveslens.data.model.Observation
import com.example.aveslens.data.remote.MlService
import com.example.aveslens.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val mlService: MlService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val observationId: String? = savedStateHandle["observationId"]
    val isEditMode: Boolean get() = observationId != null

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    // Form fields
    private val _speciesName = MutableStateFlow("")
    val speciesName: StateFlow<String> = _speciesName.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    // Image state
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _mlState = MutableStateFlow<MlState>(MlState.Idle)
    val mlState: StateFlow<MlState> = _mlState.asStateFlow()

    private var compressedBytes: ByteArray? = null
    private var existingImageUrl: String = ""

    val isDirty: Boolean
        get() = _speciesName.value.isNotBlank() ||
            _location.value.isNotBlank() ||
            _note.value.isNotBlank() ||
            _imageUri.value != null

    init {
        if (isEditMode) loadObservation()
    }

    fun onSpeciesNameChange(value: String) { _speciesName.value = value }
    fun onLocationChange(value: String) { _location.value = value }
    fun onNoteChange(value: String) { _note.value = value }

    fun pickImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _imageUri.value = uri
            val bytes = withContext(Dispatchers.IO) { ImageCompressor.compress(context, uri) }
            compressedBytes = bytes

            _mlState.value = MlState.Analyzing
            try {
                val prediction = withContext(Dispatchers.IO) { mlService.predict(bytes) }
                _mlState.value = MlState.Result(prediction.label, prediction.confidence)
                // Only auto-fill if the user hasn't typed anything yet
                if (_speciesName.value.isBlank()) {
                    _speciesName.value = prediction.label.lowercase()
                        .split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                }
            } catch (e: Exception) {
                _mlState.value = MlState.Error(e.message ?: "Classification failed")
            }
        }
    }

    fun dismissMlError() { _mlState.value = MlState.Idle }

    fun saveObservation() {
        val species = _speciesName.value.trim()
        if (species.isBlank()) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Saving
            try {
                val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = FormUiState.Error("Not logged in")
                    return@launch
                }

                // Upload image if a new one was picked
                val imageUrl = when {
                    compressedBytes != null -> {
                        val path = "$userId/${UUID.randomUUID()}.jpg"
                        supabase.storage.from("observation-images")
                            .upload(path, compressedBytes!!) { upsert = true }
                        supabase.storage.from("observation-images").publicUrl(path)
                    }
                    existingImageUrl.isNotBlank() -> existingImageUrl
                    else -> ""
                }

                if (isEditMode) {
                    supabase.from("bird_observation").update({
                        set("species_name", species)
                        set("location", _location.value.trim().ifBlank { null })
                        set("note", _note.value.trim().ifBlank { null })
                        if (imageUrl.isNotBlank()) set("image_url", imageUrl)
                    }) {
                        filter { eq("id", observationId!!) }
                    }
                } else {
                    supabase.from("bird_observation").insert(
                        Observation(
                            userId = userId,
                            speciesName = species,
                            confidenceScore = mlConfidence,
                            location = _location.value.trim().ifBlank { null },
                            note = _note.value.trim().ifBlank { null },
                            imageUrl = imageUrl,
                        )
                    )
                }

                _uiState.value = FormUiState.Success
            } catch (e: Exception) {
                _uiState.value = FormUiState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun resetState() { _uiState.value = FormUiState.Idle }


    // Also save confidence score from ML when inserting
    private var mlConfidence: Float? = null
        get() = (_mlState.value as? MlState.Result)?.confidence

    private fun loadObservation() {
        viewModelScope.launch {
            try {
                val obs = supabase.from("bird_observation")
                    .select { filter { eq("id", observationId!!) } }
                    .decodeSingleOrNull<Observation>() ?: return@launch
                _speciesName.value = obs.speciesName ?: ""
                _location.value = obs.location ?: ""
                _note.value = obs.note ?: ""
                existingImageUrl = obs.imageUrl
            } catch (_: Exception) { }
        }
    }
}

sealed class MlState {
    object Idle : MlState()
    object Analyzing : MlState()
    data class Result(val label: String, val confidence: Float) : MlState()
    data class Error(val message: String) : MlState()
}

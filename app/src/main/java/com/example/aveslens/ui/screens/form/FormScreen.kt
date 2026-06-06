package com.example.aveslens.ui.screens.form

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.aveslens.ui.components.AvesLensTextField
import com.example.aveslens.ui.components.GradientButton
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.BorderColor
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.Surface
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextHint
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import com.example.aveslens.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: FormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val speciesName by viewModel.speciesName.collectAsState()
    val location by viewModel.location.collectAsState()
    val note by viewModel.note.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val mlState by viewModel.mlState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showImagePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Temp URI for camera capture
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is FormUiState.Success -> onSaveSuccess()
            is FormUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.pickImage(context, it) }
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { viewModel.pickImage(context, it) }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraUri = createCameraUri(context)
            cameraUri?.let { cameraLauncher.launch(it) }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Camera permission denied") }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard draft?") },
            text = { Text("Your changes will not be saved.") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onNavigateBack() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            },
        )
    }

    if (showImagePicker) {
        ModalBottomSheet(
            onDismissRequest = { showImagePicker = false },
            sheetState = sheetState,
            containerColor = Surface,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Add Photo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(24.dp))
                ImageSourceRow(
                    icon = { Icon(Icons.Filled.CameraAlt, null, tint = PrimaryDark) },
                    label = "Camera",
                    onClick = {
                        showImagePicker = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraUri = createCameraUri(context)
                            cameraUri?.let { cameraLauncher.launch(it) }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
                ImageSourceRow(
                    icon = { Icon(Icons.Filled.PhotoLibrary, null, tint = PrimaryDark) },
                    label = "Gallery",
                    onClick = {
                        showImagePicker = false
                        galleryLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    if (viewModel.isDirty) showDiscardDialog = true else onNavigateBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    text = if (viewModel.isEditMode) "Edit Observation" else "New Observation",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(8.dp))

                // Image picker area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceVariant)
                        .border(
                            BorderStroke(1.dp, if (imageUri == null) BorderColor else Color.Transparent),
                            RoundedCornerShape(24.dp),
                        )
                        .clickable { showImagePicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Re-pick hint overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(PrimaryDark.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text("Change", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = TextHint,
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to add photo", style = MaterialTheme.typography.bodyMedium, color = TextHint)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ML status banner
                MlStatusBanner(
                    mlState = mlState,
                    onDismissError = viewModel::dismissMlError,
                )

                Spacer(Modifier.height(16.dp))

                // Species Name
                AvesLensTextField(
                    value = speciesName,
                    onValueChange = viewModel::onSpeciesNameChange,
                    label = "SPECIES NAME",
                    placeholder = "e.g. Common Kingfisher",
                    leadingIcon = { Icon(Icons.Filled.Edit, null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // Location
                AvesLensTextField(
                    value = location,
                    onValueChange = viewModel::onLocationChange,
                    label = "LOCATION",
                    placeholder = "e.g. Kinabalu Park, Sabah",
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // Field Notes
                AvesLensTextField(
                    value = note,
                    onValueChange = viewModel::onNoteChange,
                    label = "FIELD NOTES",
                    placeholder = "Describe the sighting — behaviour, surroundings, distinguishing features…",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default,
                    ),
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Field photography tips (shown only when no image selected)
                if (imageUri == null) {
                    Spacer(Modifier.height(24.dp))
                    PhotographyTips()
                }

                Spacer(Modifier.height(28.dp))

                GradientButton(
                    text = "Save Observation ✓",
                    onClick = viewModel::saveObservation,
                    enabled = speciesName.isNotBlank(),
                    isLoading = uiState is FormUiState.Saving,
                )

                Spacer(Modifier.height(12.dp))

                TextButton(
                    onClick = { if (viewModel.isDirty) showDiscardDialog = true else onNavigateBack() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Discard Draft", color = TextTertiary, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

@Composable
private fun ImageSourceRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PhotographyTips() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Field Photography Tips",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
        )
        TipRow(
            icon = { Icon(Icons.Filled.LightMode, null, tint = PrimaryDark, modifier = Modifier.size(18.dp)) },
            text = "Shoot with the light source behind you for clear plumage colours.",
        )
        TipRow(
            icon = { Icon(Icons.Filled.Star, null, tint = PrimaryDark, modifier = Modifier.size(18.dp)) },
            text = "Focus on the eye — a sharp eye makes identification far easier.",
        )
    }
}

@Composable
private fun TipRow(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

// ─── ML Status Banner ─────────────────────────────────────────────────────────

@Composable
private fun MlStatusBanner(
    mlState: MlState,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (mlState) {
        is MlState.Idle -> Unit

        is MlState.Analyzing -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(SurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryDark,
                )
                Text(
                    "Identifying species…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }

        is MlState.Result -> {
            val pct = (mlState.confidence * 100).toInt()
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(PrimaryDark.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.size(18.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Species identified",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryDark,
                    )
                    Text(
                        "$pct% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        is MlState.Error -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "Could not identify species",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismissError) {
                    Text("OK", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun createCameraUri(context: Context): Uri {
    val file = File(context.getExternalFilesDir("Pictures"), "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

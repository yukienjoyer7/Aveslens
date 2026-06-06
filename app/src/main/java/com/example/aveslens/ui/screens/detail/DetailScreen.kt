package com.example.aveslens.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aveslens.ui.components.GradientButton
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.BorderColor
import com.example.aveslens.ui.theme.OnPrimary
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.Surface
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import com.example.aveslens.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteSuccess: () -> Unit,
    observationUpdated: Boolean = false,
    onObservationUpdatedConsumed: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(observationUpdated) {
        if (observationUpdated) {
            viewModel.loadObservation()
            onObservationUpdatedConsumed()
        }
    }

    LaunchedEffect(deleteState) {
        when (val s = deleteState) {
            is DeleteState.Success -> onDeleteSuccess()
            is DeleteState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete this observation?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteObservation()
                }) {
                    Text(
                        if (deleteState is DeleteState.Deleting) "Deleting…" else "Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryDark,
                )
            }

            is DetailUiState.NotFound -> {
                NotFoundContent(onNavigateBack = onNavigateBack)
            }

            is DetailUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::loadObservation,
                    onNavigateBack = onNavigateBack,
                )
            }

            is DetailUiState.Success -> {
                val obs = state.observation

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Hero image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                    ) {
                        // Back button overlay
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(obs.imageUrl.ifBlank { null })
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(SurfaceVariant),
                            error = ColorPainter(SurfaceVariant),
                            modifier = Modifier.fillMaxSize(),
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                        startY = 120f,
                                    )
                                ),
                        )
                        // "Observed X ago" label
                        Text(
                            text = "Observed ${formatRelativeTime(obs.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 20.dp, bottom = 64.dp),
                        )
                    }

                    // Content card overlapping the image by 48dp
                    M3Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-48).dp),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        color = Background,
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 24.dp, end = 24.dp,
                                top = 24.dp, bottom = 96.dp,
                            ),
                        ) {
                            // Species name
                            Text(
                                text = obs.speciesName ?: "Unknown Species",
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary,
                            )

                            // Confidence chip
                            if (obs.confidenceScore != null) {
                                Spacer(Modifier.height(12.dp))
                                ConfidenceChip(score = obs.confidenceScore)
                            }

                            Spacer(Modifier.height(28.dp))

                            // Info cards
                            if (!obs.location.isNullOrBlank()) {
                                InfoCard(
                                    icon = { Icon(Icons.Filled.LocationOn, null, tint = PrimaryDark, modifier = Modifier.size(18.dp)) },
                                    label = "Location",
                                    value = obs.location,
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            InfoCard(
                                icon = { Icon(Icons.Filled.CalendarToday, null, tint = PrimaryDark, modifier = Modifier.size(16.dp)) },
                                label = "Date",
                                value = formatDetailDate(obs.createdAt),
                            )

                            if (!obs.note.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                InfoCard(
                                    icon = { Icon(Icons.AutoMirrored.Filled.Notes, null, tint = PrimaryDark, modifier = Modifier.size(18.dp)) },
                                    label = "Field Notes",
                                    value = obs.note,
                                )
                            }
                        }
                    }
                }

                // FABs — stacked at bottom right
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SmallFloatingActionButton(
                        onClick = { showDeleteDialog = true },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                    FloatingActionButton(
                        onClick = { onNavigateToEdit(obs.id) },
                        containerColor = PrimaryDark,
                        contentColor = OnPrimary,
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(22.dp))
                    }
                }

            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

@Composable
private fun InfoCard(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    M3Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun ConfidenceChip(score: Float) {
    val pct = (score * 100).toInt()
    val color = when {
        pct >= 80 -> PrimaryDark
        pct >= 50 -> TextTertiary
        else -> MaterialTheme.colorScheme.error
    }
    Text(
        text = "$pct% confidence",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(color, RoundedCornerShape(9999.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun NotFoundContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Star, null, tint = SurfaceVariant, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Observation no longer exists",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "It may have been deleted from another session.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(text = "Go Back", onClick = onNavigateBack)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Couldn't load observation", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        GradientButton(text = "Retry", onClick = onRetry)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateBack) {
            Text("Go Back", color = TextTertiary)
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatRelativeTime(dateStr: String?): String {
    if (dateStr == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr.take(19)) ?: return ""
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000 -> "just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 7 * 86_400_000 -> "${diff / 86_400_000}d ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) { "" }
}

private fun formatDetailDate(dateStr: String?): String {
    if (dateStr == null) return ""
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = input.parse(dateStr.take(19)) ?: return dateStr.take(10)
        val part1 = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        val part2 = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        val part3 = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        "$part1 / $part2 / $part3"
    } catch (e: Exception) { dateStr.take(10) }
}

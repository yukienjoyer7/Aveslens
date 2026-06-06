package com.example.aveslens.ui.screens.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aveslens.ui.components.AvesLensBottomNavBar
import com.example.aveslens.ui.components.AvesLensTopAppBar
import com.example.aveslens.ui.components.GradientButton
import com.example.aveslens.ui.components.ObservationCard
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.BorderColor
import com.example.aveslens.ui.theme.OnPrimary
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextHint
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import com.example.aveslens.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToForm: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExplorer: () -> Unit,
    observationSaved: Boolean = false,
    onObservationSavedConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val greeting by viewModel.greeting.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(observationSaved) {
        if (observationSaved) {
            viewModel.refresh()
            onObservationSavedConsumed()
        }
    }

    Scaffold(
        topBar = { AvesLensTopAppBar(onAvatarClick = onNavigateToProfile) },
        bottomBar = {
            AvesLensBottomNavBar(
                currentRoute = "home",
                onJournalClick = { /* already here */ },
                onObserveClick = onNavigateToForm,
                onExplorerClick = onNavigateToExplorer,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToForm,
                containerColor = PrimaryDark,
                contentColor = OnPrimary,
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Observation", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = Background,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> ShimmerLoadingContent()
                is HomeUiState.Empty -> EmptyStateContent(onRecordFirstBird = onNavigateToForm)
                is HomeUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = viewModel::loadObservations,
                )
                is HomeUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Header + search bar always visible
                        item {
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = "$greeting, $displayName!",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Track your bird sightings",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary,
                                )
                                Spacer(Modifier.height(20.dp))
                                SearchBar(
                                    value = searchQuery,
                                    onValueChange = viewModel::onSearchQueryChange,
                                )
                                Spacer(Modifier.height(28.dp))
                                Text(
                                    text = "Recent Observations",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary,
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        if (state.observations.isEmpty()) {
                            // Search yielded no matches
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = PrimaryDark.copy(alpha = 0.4f),
                                        modifier = Modifier.size(56.dp),
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = "No results found",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Try a different species or location.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        } else {
                            items(state.observations, key = { it.id }) { observation ->
                                ObservationCard(
                                    observation = observation,
                                    onClick = { onNavigateToDetail(observation.id) },
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Search Bar ─────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = TextHint,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            cursorBrush = SolidColor(PrimaryDark),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Search by species or location…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextHint,
                    )
                }
                inner()
            },
        )
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateContent(
    onRecordFirstBird: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Illustration placeholder — Phase 9 replaces with actual bird SVG
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(SurfaceVariant, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiNature,
                contentDescription = null,
                tint = PrimaryDark.copy(alpha = 0.6f),
                modifier = Modifier.size(72.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Your journal is empty",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Start your journey by recording your first bird sighting.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        GradientButton(
            text = "Record First Bird",
            onClick = onRecordFirstBird,
            icon = { Icon(Icons.Filled.Camera, contentDescription = null, tint = OnPrimary) },
        )

    }
}

// ─── Error State ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Couldn't load observations",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(text = "Retry", onClick = onRetry)
    }
}

// ─── Shimmer / Skeleton Loading ───────────────────────────────────────────────

@Composable
private fun ShimmerLoadingContent(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -600f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmerX",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE8E7E5),
            Color(0xFFF4F3F1),
            Color(0xFFE8E7E5),
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 300f, 300f),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        // Greeting skeleton
        ShimmerBox(shimmerBrush, Modifier.fillMaxWidth(0.6f).height(36.dp))
        Spacer(Modifier.height(8.dp))
        ShimmerBox(shimmerBrush, Modifier.fillMaxWidth(0.4f).height(20.dp))
        Spacer(Modifier.height(20.dp))
        // Search bar skeleton
        ShimmerBox(shimmerBrush, Modifier.fillMaxWidth().height(50.dp))
        Spacer(Modifier.height(28.dp))
        ShimmerBox(shimmerBrush, Modifier.fillMaxWidth(0.5f).height(28.dp))
        Spacer(Modifier.height(16.dp))
        repeat(2) {
            SkeletonCard(shimmerBrush)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SkeletonCard(brush: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3F1), RoundedCornerShape(32.dp)),
    ) {
        ShimmerBox(brush, Modifier.fillMaxWidth().height(224.dp).background(Color.Transparent, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)))
        Column(Modifier.padding(20.dp)) {
            ShimmerBox(brush, Modifier.fillMaxWidth(0.65f).height(22.dp))
            Spacer(Modifier.height(10.dp))
            ShimmerBox(brush, Modifier.fillMaxWidth(0.45f).height(16.dp))
        }
    }
}

@Composable
private fun ShimmerBox(brush: Brush, modifier: Modifier) {
    Box(modifier = modifier.background(brush, RoundedCornerShape(8.dp)))
}

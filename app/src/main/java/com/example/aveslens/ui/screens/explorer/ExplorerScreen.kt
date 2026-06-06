package com.example.aveslens.ui.screens.explorer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aveslens.ui.components.AvesLensBottomNavBar
import com.example.aveslens.ui.components.AvesLensTopAppBar
import com.example.aveslens.ui.components.SpeciesRow
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextHint
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary

@Composable
fun ExplorerScreen(
    onNavigateToJournal: () -> Unit,
    onNavigateToForm: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = { AvesLensTopAppBar(onAvatarClick = onNavigateToProfile) },
        bottomBar = {
            AvesLensBottomNavBar(
                currentRoute = "explorer",
                onJournalClick = onNavigateToJournal,
                onObserveClick = onNavigateToForm,
                onExplorerClick = { },
            )
        },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            when (val state = uiState) {
                is ExplorerUiState.Success -> {
                    val totalSpecies = if (searchQuery.isBlank())
                        state.species.size
                    else
                        viewModel.uiState.value.let {
                            (it as? ExplorerUiState.Success)?.species?.size ?: 0
                        }
                    HeaderRow(speciesCount = totalSpecies, searchQuery = searchQuery)
                }
                else -> HeaderRow(speciesCount = null, searchQuery = searchQuery)
            }

            Spacer(Modifier.height(16.dp))

            // Search bar
            SearchBar(value = searchQuery, onValueChange = viewModel::onSearchQueryChange)

            Spacer(Modifier.height(20.dp))

            // Content
            when (val state = uiState) {
                is ExplorerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                is ExplorerUiState.Empty -> {
                    EmptyState(
                        message = "No species yet",
                        sub = "Start observing to build your personal field guide.",
                    )
                }

                is ExplorerUiState.Error -> {
                    EmptyState(message = "Couldn't load species", sub = state.message)
                }

                is ExplorerUiState.Success -> {
                    if (state.species.isEmpty()) {
                        EmptyState(
                            message = "No results for \"$searchQuery\"",
                            sub = "Try a different search term.",
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 88.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(state.species, key = { it.name }) { species ->
                                SpeciesRow(species = species)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

@Composable
private fun HeaderRow(speciesCount: Int?, searchQuery: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "My Species",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )
        if (speciesCount != null && searchQuery.isBlank()) {
            Text(
                "$speciesCount ${if (speciesCount == 1) "Species" else "Species"} Identified",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryDark,
            )
        }
    }
}

@Composable
private fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant, RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, null, tint = TextHint, modifier = Modifier.size(18.dp))
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
                    Text("Search species…", style = MaterialTheme.typography.bodyLarge, color = TextHint)
                }
                inner()
            },
        )
    }
}

@Composable
private fun EmptyState(message: String, sub: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.EmojiNature,
            contentDescription = null,
            tint = PrimaryDark.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(sub, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

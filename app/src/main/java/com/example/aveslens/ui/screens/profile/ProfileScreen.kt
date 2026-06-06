package com.example.aveslens.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.ImeAction
import com.example.aveslens.ui.components.AvesLensTextField
import com.example.aveslens.ui.components.GradientButton
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aveslens.BuildConfig
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.BorderColor
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.Surface
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import com.example.aveslens.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showEditSheet by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        if (logoutState) onLogout()
    }

    // Edit Profile bottom sheet
    if (showEditSheet) {
        val currentProfile = (uiState as? ProfileUiState.Success)?.profile
        var fullName by remember { mutableStateOf(currentProfile?.fullName ?: "") }
        var username by remember { mutableStateOf(currentProfile?.username ?: "") }

        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            containerColor = com.example.aveslens.ui.theme.Surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .imePadding(),
            ) {
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(24.dp))
                AvesLensTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "FULL NAME",
                    placeholder = "Your full name",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                AvesLensTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "USERNAME",
                    placeholder = "birder_username",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
                GradientButton(
                    text = "Save Changes",
                    onClick = {
                        viewModel.updateProfile(fullName, username)
                        showEditSheet = false
                    },
                    enabled = fullName.isNotBlank(),
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showEditSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Cancel", color = TextTertiary)
                }
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
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    "Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
            }

            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.message, color = TextSecondary, textAlign = TextAlign.Center)
                    }
                }

                is ProfileUiState.Success -> {
                    val profile = state.profile
                    val stats = state.stats

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(24.dp))

                        // Avatar — initials
                        val initial = profile.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        Box(
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(PrimaryDark),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = initial,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Full name
                        Text(
                            text = profile.fullName.ifBlank { "Unnamed Birder" },
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(4.dp))

                        // Username
                        Text(
                            text = if (profile.username.isBlank()) "" else "@${profile.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )

                        Spacer(Modifier.height(32.dp))

                        // Stats row — IntrinsicSize.Min ensures both cards match the taller one
                        Row(
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            StatCard(
                                count = stats.birdsObserved,
                                label = "BIRDS\nOBSERVED",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                            StatCard(
                                count = stats.speciesIdentified,
                                label = "SPECIES\nIDENTIFIED",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }

                        Spacer(Modifier.height(36.dp))

                        // Account Settings section
                        Text(
                            "Account Settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        )

                        SettingsRow(icon = Icons.Filled.Edit, label = "Edit Profile", onClick = { showEditSheet = true })
                        SettingsRow(icon = Icons.Filled.Lock, label = "Privacy", onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Privacy settings coming soon") }
                        })
                        SettingsRow(icon = Icons.AutoMirrored.Filled.Help, label = "Help", onClick = {
                            scope.launch { snackbarHostState.showSnackbar("Help & support coming soon") }
                        })

                        Spacer(Modifier.height(32.dp))

                        // Logout button
                        OutlinedButton(
                            onClick = viewModel::logout,
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Logout",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Version ${BuildConfig.VERSION_NAME} — Proudly Open Source",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ─── Supporting Composables ───────────────────────────────────────────────────

@Composable
private fun StatCard(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(20.dp))
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryDark,
            lineHeight = 44.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.8.sp,
            ),
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SurfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderColor),
    )
}

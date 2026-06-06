package com.example.aveslens.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.painterResource
import com.example.aveslens.R
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aveslens.ui.components.AvesLensTextField
import com.example.aveslens.ui.components.GradientButton
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.CardShape
import com.example.aveslens.ui.theme.Primary
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.PrimaryLight
import com.example.aveslens.ui.theme.Surface
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import com.example.aveslens.ui.theme.TextTertiary

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Navigate to home on success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onNavigateToHome()
        }
        if (uiState is AuthUiState.Error) {
            val msg = (uiState as AuthUiState.Error).message
            if (msg.contains("No internet", ignoreCase = true)) {
                snackbarHostState.showSnackbar(msg)
                viewModel.resetState()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.22f), Background.copy(alpha = 0f)),
                    ),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryLight.copy(alpha = 0.18f), Background.copy(alpha = 0f)),
                    ),
                    CircleShape,
                ),
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(88.dp))

            // Logo
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = PrimaryDark,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AvesLens",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryDark,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your Personal Bird Journal",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Auth card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                ) {
                    // Card heading
                    Text(
                        text = if (isLoginMode) "Welcome Back" else "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Email
                    AvesLensTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        error = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Register-only fields
                    if (!isLoginMode) {
                        AvesLensTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "FULL NAME",
                            placeholder = "Your full name",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AvesLensTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "USERNAME",
                            placeholder = "birder_username",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Password
                    AvesLensTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = "PASSWORD",
                        placeholder = "Min. 8 characters",
                        isPassword = true,
                        error = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // API error (non-network)
                    if (uiState is AuthUiState.Error) {
                        val msg = (uiState as AuthUiState.Error).message
                        if (!msg.contains("No internet", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary action button
                    GradientButton(
                        text = if (isLoginMode) "Login" else "Create Account",
                        isLoading = uiState is AuthUiState.Loading,
                        onClick = {
                            if (validate(email, password, onEmailError = { emailError = it }, onPasswordError = { passwordError = it })) {
                                if (isLoginMode) viewModel.login(email, password)
                                else viewModel.register(email, password, fullName, username)
                            }
                        },
                    )

                    // Forgot password (login only)
                    if (isLoginMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { /* Phase 9 */ },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode toggle
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (isLoginMode) "Don't have an account? " else "Already have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        TextButton(onClick = {
                            isLoginMode = !isLoginMode
                            viewModel.resetState()
                            emailError = null
                            passwordError = null
                        }) {
                            Text(
                                text = if (isLoginMode) "Register" else "Login",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryDark,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private fun validate(
    email: String,
    password: String,
    onEmailError: (String) -> Unit,
    onPasswordError: (String) -> Unit,
): Boolean {
    var valid = true
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Enter a valid email address")
        valid = false
    }
    if (password.length < 8) {
        onPasswordError("Password must be at least 8 characters")
        valid = false
    }
    return valid
}

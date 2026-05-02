package com.medvision.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.medvision.ai.data.model.APP_DISCLAIMER
import com.medvision.ai.ui.components.DisclaimerBanner
import com.medvision.ai.ui.components.GlassCard
import com.medvision.ai.ui.components.LoadingCard
import com.medvision.ai.ui.components.PrimaryActionButton
import com.medvision.ai.ui.components.ScreenHeader
import com.medvision.ai.viewmodel.AuthUiState

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        ScreenHeader(
            title = "MedVision AI",
            subtitle = "Premium AI-assisted health insights with secure sign-in."
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (state.isLoginMode) "Welcome back" else "Create account",
                    style = MaterialTheme.typography.titleLarge
                )
                if (!state.isLoginMode) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                PrimaryActionButton(
                    text = if (state.isLoginMode) "Login" else "Sign Up",
                    onClick = onSubmit,
                    enabled = !state.isLoading
                )
                TextButton(onClick = onToggleMode, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isLoginMode) "Need an account? Sign up" else "Already have an account? Login")
                }
            }
        }
        if (state.isLoading) {
            LoadingCard(message = "Securing your session...")
        }
        DisclaimerBanner(text = APP_DISCLAIMER)
    }
}

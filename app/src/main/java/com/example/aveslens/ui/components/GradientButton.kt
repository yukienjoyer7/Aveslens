package com.example.aveslens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aveslens.ui.theme.ButtonShape
import com.example.aveslens.ui.theme.OnPrimary
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.PrimaryLight
import com.example.aveslens.ui.theme.TextHint

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val gradient = if (enabled) {
        Brush.linearGradient(
            colors = listOf(PrimaryDark, PrimaryLight),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY),
        )
    } else {
        Brush.linearGradient(colors = listOf(TextHint, TextHint))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = ButtonShape,
                ambientColor = PrimaryDark.copy(alpha = 0.12f),
                spotColor = PrimaryDark.copy(alpha = 0.12f),
            )
            .clip(ButtonShape)
            .background(gradient)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = OnPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else if (icon != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) OnPrimary else Color.White.copy(alpha = 0.6f),
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OnPrimary else Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

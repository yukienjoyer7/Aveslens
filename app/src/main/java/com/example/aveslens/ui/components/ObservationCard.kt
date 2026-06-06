package com.example.aveslens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aveslens.data.model.Observation
import com.example.aveslens.ui.theme.BorderColor
import com.example.aveslens.ui.theme.CardShape
import com.example.aveslens.ui.theme.OnPrimary
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.Surface
import com.example.aveslens.ui.theme.SurfaceVariant
import com.example.aveslens.ui.theme.TextPrimary
import com.example.aveslens.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ObservationCard(
    observation: Observation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column {
            // Image section with optional RARE badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(observation.imageUrl.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(SurfaceVariant),
                    error = ColorPainter(SurfaceVariant),
                    modifier = Modifier.matchParentSize(),
                )

                // RARE badge — re-enable when is_rare column is added to DB

            }

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = observation.speciesName ?: "Unknown Species",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!observation.location.isNullOrBlank()) {
                        MetaChip(
                            icon = { Icon(Icons.Filled.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(14.dp)) },
                            label = observation.location,
                        )
                    }
                    if (!observation.createdAt.isNullOrBlank()) {
                        MetaChip(
                            icon = { Icon(Icons.Filled.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(12.dp)) },
                            label = formatObservationDate(observation.createdAt),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RareBadge(modifier: Modifier = Modifier) {
    Text(
        text = "RARE",
        style = MaterialTheme.typography.labelSmall,
        color = OnPrimary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = modifier
            .background(PrimaryDark, RoundedCornerShape(9999.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun MetaChip(
    icon: @Composable () -> Unit,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

private fun formatObservationDate(dateStr: String): String {
    return try {
        // Supabase returns ISO 8601: "2023-10-24T08:42:00+00:00"
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        output.format(input.parse(dateStr.take(19)) ?: return dateStr.take(10))
    } catch (e: Exception) {
        dateStr.take(10)
    }
}

package com.example.healthypettracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class TimelineEntry(
    val dateTime: LocalDateTime,
    val catName: String
) {
    class Weight(
        dateTime: LocalDateTime,
        catName: String,
        val catId: Long,
        val weightGrams: Int,
        val notes: String?
    ) : TimelineEntry(dateTime, catName)

    class Food(
        dateTime: LocalDateTime,
        catName: String,
        val foodType: String,
        val brandName: String?,
        val amountGrams: Int?
    ) : TimelineEntry(dateTime, catName)

    class Medicine(
        dateTime: LocalDateTime,
        catName: String,
        val medicineName: String,
        val wasSkipped: Boolean,
        val isScheduled: Boolean = false
    ) : TimelineEntry(dateTime, catName)

    class Diary(
        dateTime: LocalDateTime,
        catName: String,
        val title: String,
        val content: String?,
        val category: String,
        val noteId: Long
    ) : TimelineEntry(dateTime, catName)
}

@Composable
fun TimelineItemCard(
    entry: TimelineEntry,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isScheduled = entry is TimelineEntry.Medicine && entry.isScheduled
    val dashedBorderColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isScheduled) {
                    Modifier.drawWithContent {
                        drawContent()
                        val strokeWidth = 2.dp.toPx()
                        val cornerRadiusPx = 12.dp.toPx()
                        drawRoundRect(
                            color = dashedBorderColor,
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = if (isScheduled) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ) else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isScheduled) 0.dp else 2.dp),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            TimelineIcon(entry = entry)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getEntryTitle(entry),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = entry.catName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = getEntrySubtitle(entry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = entry.dateTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimelineIcon(entry: TimelineEntry) {
    val (icon, color) = when (entry) {
        is TimelineEntry.Weight -> Icons.Default.Favorite to MaterialTheme.colorScheme.error
        is TimelineEntry.Food -> Icons.Default.ShoppingCart to MaterialTheme.colorScheme.tertiary
        is TimelineEntry.Medicine -> Icons.Default.Favorite to MaterialTheme.colorScheme.primary
        is TimelineEntry.Diary -> Icons.Default.Create to MaterialTheme.colorScheme.secondary
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

private fun getEntryTitle(entry: TimelineEntry): String {
    return when (entry) {
        is TimelineEntry.Weight -> "Weight: ${String.format("%.2f kg", entry.weightGrams / 1000.0)}"
        is TimelineEntry.Food -> "${entry.foodType}${entry.amountGrams?.let { " - ${it}g" } ?: ""}"
        is TimelineEntry.Medicine -> when {
            entry.isScheduled -> "${entry.medicineName} (Scheduled)"
            entry.wasSkipped -> "${entry.medicineName} (Skipped)"
            else -> entry.medicineName
        }
        is TimelineEntry.Diary -> entry.title
    }
}

private fun getEntrySubtitle(entry: TimelineEntry): String {
    return when (entry) {
        is TimelineEntry.Weight -> entry.notes ?: "Weight recorded"
        is TimelineEntry.Food -> entry.brandName ?: "Food logged"
        is TimelineEntry.Medicine -> when {
            entry.isScheduled -> "Medicine due"
            entry.wasSkipped -> "Medicine skipped"
            else -> "Medicine given"
        }
        is TimelineEntry.Diary -> entry.content?.take(100) ?: entry.category
    }
}

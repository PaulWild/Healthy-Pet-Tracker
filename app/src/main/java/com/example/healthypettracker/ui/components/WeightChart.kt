package com.example.healthypettracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.healthypettracker.data.local.entity.WeightEntry

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    if (entries.size < 2) {
        return
    }

    // Sort entries by date (oldest first)
    val sortedEntries = entries.sortedBy { it.measuredAt }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Find min and max weights for scaling
        val weights = sortedEntries.map { it.weightGrams }
        val minWeight = (weights.minOrNull() ?: 0) * 0.95
        val maxWeight = (weights.maxOrNull() ?: 1) * 1.05
        val weightRange = maxWeight - minWeight

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padding + (chartHeight * i / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Calculate points
        val points = sortedEntries.mapIndexed { index, entry ->
            val x = padding + (chartWidth * index / (sortedEntries.size - 1))
            val y = padding + chartHeight - (chartHeight * (entry.weightGrams - minWeight) / weightRange).toFloat()
            Offset(x, y)
        }

        // Draw line connecting points
        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = pointColor,
                radius = 6f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = point
            )
        }
    }
}

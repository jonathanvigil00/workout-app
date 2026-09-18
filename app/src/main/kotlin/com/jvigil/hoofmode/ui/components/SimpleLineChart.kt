package com.jvigil.hoofmode.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.abs

data class ChartPoint(val x: Float, val label: String, val y: Float, val valueText: String)

/**
 * A minimal, dependency-free line chart: draws the series, and lets the viewer tap a point to
 * see its exact value/date (requirement 7.6). Renders an empty-state message when there's
 * fewer than two points to connect.
 */
@Composable
fun SimpleLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    emptyMessage: String = "Not enough data yet.",
) {
    if (points.size < 2) {
        Column(modifier = modifier.fillMaxWidth().height(180.dp).padding(16.dp)) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selected by remember(points) { mutableStateOf<ChartPoint?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val yRange = (maxY - minY).takeIf { it > 0f } ?: 1f
    val xRange = (maxX - minX).takeIf { it > 0f } ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val nearest = points.minByOrNull { p ->
                            val px = (p.x - minX) / xRange * size.width
                            abs(px - tapOffset.x)
                        }
                        selected = nearest
                    }
                },
        ) {
            val w = size.width
            val h = size.height
            val path = androidx.compose.ui.graphics.Path()
            points.forEachIndexed { index, p ->
                val px = (p.x - minX) / xRange * w
                val py = h - (p.y - minY) / yRange * h
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = StrokeCap.Round))

            points.forEach { p ->
                val px = (p.x - minX) / xRange * w
                val py = h - (p.y - minY) / yRange * h
                drawCircle(color = lineColor, radius = if (p == selected) 8f else 4f, center = Offset(px, py))
            }

            val maxLabel = textMeasurer.measure(String.format("%.1f", maxY))
            drawText(textMeasurer, String.format("%.1f", maxY), topLeft = Offset(0f, 0f))
            drawText(textMeasurer, String.format("%.1f", minY), topLeft = Offset(0f, h - maxLabel.size.height))
        }

        Text(
            selected?.let { "${it.label}: ${it.valueText}" } ?: "Tap a point for details",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

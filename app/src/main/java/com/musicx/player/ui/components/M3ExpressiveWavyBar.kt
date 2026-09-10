package com.musicx.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun M3ExpressiveWavyBar(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    val waveTransition = rememberInfiniteTransition(label = "waveTransition")
    val waveOffset by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
    ) {
        val width = size.width
        val midY = size.height / 2
        val activeWidth = width * progress.coerceIn(0f, 1f)

        // Inactive flat track
        drawLine(
            color = inactiveColor,
            start = Offset(activeWidth, midY),
            end = Offset(width, midY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        // Active Animated Wavy Path
        val path = Path()
        val freq = 0.05f
        val amp = if (isPlaying) 10f else 0f

        path.moveTo(0f, midY)
        var x = 0f
        while (x <= activeWidth) {
            val y = midY + sin((x * freq) + Math.toRadians(waveOffset.toDouble())).toFloat() * amp
            path.lineTo(x, y)
            x += 2f
        }

        drawPath(
            path = path,
            color = activeColor,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Squircle Knob
        drawCircle(
            color = activeColor,
            radius = 14f,
            center = Offset(activeWidth, midY)
        )
    }
}

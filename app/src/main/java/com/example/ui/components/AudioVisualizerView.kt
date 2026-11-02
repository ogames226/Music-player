package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audio.VisualizerState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldLight

@Composable
fun AudioVisualizerView(
    visualizerState: VisualizerState,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    barColorGradient: List<Color> = listOf(VipGold, ElectricCyan, NeonPurple)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val barCount = visualizerState.bars.size
        if (barCount == 0) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height
        val totalSpacing = canvasWidth * 0.25f
        val barSpacing = totalSpacing / (barCount - 1).coerceAtLeast(1)
        val barWidth = (canvasWidth - totalSpacing) / barCount

        val brush = Brush.verticalGradient(
            colors = listOf(
                VipGoldLight,
                VipGold,
                ElectricCyan
            ),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until barCount) {
            val normalizedHeight = visualizerState.bars[i].coerceIn(0.08f, 1.0f)
            val currentBarHeight = canvasHeight * normalizedHeight
            val left = i * (barWidth + barSpacing)
            val top = canvasHeight - currentBarHeight

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

package com.example.audio

import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

data class VisualizerState(
    val bars: List<Float> = List(24) { 0.1f },
    val peakAmplitude: Float = 0.5f,
    val bassIntensity: Float = 0.3f
) {
    companion object {
        fun generateFrame(isPlaying: Boolean, timeTick: Long, bassBoost: Float): VisualizerState {
            if (!isPlaying) {
                return VisualizerState(bars = List(24) { 0.08f }, peakAmplitude = 0.1f, bassIntensity = 0.1f)
            }
            val t = timeTick.toFloat() * 0.15f
            val boostMult = 1f + (bassBoost / 100f) * 0.5f

            val generatedBars = List(24) { index ->
                val freq = (index + 1) * 0.4f
                val wave1 = abs(sin(t * 0.8f + freq))
                val wave2 = abs(sin(t * 1.3f - freq * 0.5f))
                val jitter = Random.nextFloat() * 0.15f
                val bassWeight = if (index < 6) boostMult else 1.0f
                val rawHeight = ((wave1 * 0.6f + wave2 * 0.3f + jitter) * bassWeight).coerceIn(0.12f, 1.0f)
                rawHeight
            }

            val peak = generatedBars.maxOrNull() ?: 0.5f
            val bass = (generatedBars.take(6).average()).toFloat()

            return VisualizerState(
                bars = generatedBars,
                peakAmplitude = peak,
                bassIntensity = bass
            )
        }
    }
}

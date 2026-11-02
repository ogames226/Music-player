package com.example.model

data class EqualizerPreset(
    val name: String,
    val bandLevels: List<Float>, // 10 bands from -12dB to +12dB
    val bassBoost: Float = 0f,    // 0 to 100
    val virtualizer: Float = 0f,  // 0 to 100
    val isVipExclusive: Boolean = false
) {
    companion object {
        val BAND_LABELS = listOf(
            "31 Hz", "62 Hz", "125 Hz", "250 Hz", "500 Hz",
            "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz"
        )

        val DEFAULT_PRESETS = listOf(
            EqualizerPreset(
                name = "Flat (Default)",
                bandLevels = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
                bassBoost = 0f,
                virtualizer = 0f,
                isVipExclusive = false
            ),
            EqualizerPreset(
                name = "Pop",
                bandLevels = listOf(-1f, 1f, 3f, 4f, 2f, -1f, -1f, 1f, 3f, 4f),
                bassBoost = 35f,
                virtualizer = 20f,
                isVipExclusive = false
            ),
            EqualizerPreset(
                name = "Rock",
                bandLevels = listOf(5f, 3f, -1f, -2f, 1f, 3f, 5f, 6f, 5f, 4f),
                bassBoost = 40f,
                virtualizer = 25f,
                isVipExclusive = false
            ),
            EqualizerPreset(
                name = "Jazz",
                bandLevels = listOf(2f, 3f, 1f, 2f, -1f, -1f, 1f, 2f, 3f, 4f),
                bassBoost = 20f,
                virtualizer = 30f,
                isVipExclusive = false
            ),
            EqualizerPreset(
                name = "Classical",
                bandLevels = listOf(4f, 3f, 2f, 1f, -1f, -1f, 0f, 2f, 4f, 5f),
                bassBoost = 15f,
                virtualizer = 40f,
                isVipExclusive = false
            ),
            EqualizerPreset(
                name = "Electronic / EDM",
                bandLevels = listOf(7f, 6f, 4f, 1f, -2f, 2f, 4f, 6f, 7f, 8f),
                bassBoost = 65f,
                virtualizer = 50f,
                isVipExclusive = false
            ),
            // VIP EXCLUSIVE PRESETS:
            EqualizerPreset(
                name = "VIP Mega Bass Pro",
                bandLevels = listOf(11f, 10f, 8f, 5f, 1f, 0f, 2f, 5f, 8f, 9f),
                bassBoost = 90f,
                virtualizer = 60f,
                isVipExclusive = true
            ),
            EqualizerPreset(
                name = "VIP 3D Surround Master",
                bandLevels = listOf(4f, 3f, 2f, 3f, 5f, 6f, 7f, 9f, 10f, 11f),
                bassBoost = 50f,
                virtualizer = 95f,
                isVipExclusive = true
            ),
            EqualizerPreset(
                name = "VIP Audiophile Studio HQ",
                bandLevels = listOf(3f, 2f, 1f, 2f, 4f, 5f, 3f, 2f, 4f, 6f),
                bassBoost = 45f,
                virtualizer = 70f,
                isVipExclusive = true
            )
        )
    }
}

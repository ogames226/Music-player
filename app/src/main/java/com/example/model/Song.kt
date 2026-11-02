package com.example.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val path: String = "",
    val albumArtUri: String? = null,
    val genre: String = "Pop",
    val bitrate: String = "320 kbps",
    val format: String = "MP3",
    val fileSizeBytes: Long = 8_420_000L,
    val isFavorite: Boolean = false,
    val lyrics: List<LyricLine> = emptyList(),
    val isSynthTrack: Boolean = true,
    val synthScale: List<Float> = listOf(440f, 493.88f, 523.25f, 587.33f, 659.25f)
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = fileSizeBytes.toDouble() / (1024 * 1024)
            return String.format("%.1f MB", mb)
        }
}

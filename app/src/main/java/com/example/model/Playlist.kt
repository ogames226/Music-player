package com.example.model

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val iconName: String = "playlist"
)

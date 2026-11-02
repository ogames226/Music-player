package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.RepeatMode
import com.example.audio.VisualizerState
import com.example.model.Song
import com.example.model.VipStatus
import com.example.ui.components.AudioVisualizerView
import com.example.ui.components.VipBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VipGold
import com.example.ui.theme.VipGoldLight

enum class PlayerViewMode {
    VINYL, VISUALIZER, LYRICS
}

@Composable
fun NowPlayingSheet(
    isOpen: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    activeLyricIndex: Int,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    playbackSpeed: Float,
    visualizerState: VisualizerState,
    vipStatus: VipStatus,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenTagEditor: (Song) -> Unit,
    onOpenVipDialog: () -> Unit
) {
    if (!isOpen || currentSong == null) return

    var viewMode by remember { mutableStateOf(PlayerViewMode.VINYL) }
    val lyricsListState = rememberLazyListState()

    // Smoothly scroll to active lyric
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && viewMode == PlayerViewMode.LYRICS) {
            lyricsListState.animateScrollToItem((activeLyricIndex - 1).coerceAtLeast(0))
        }
    }

    // Vinyl rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "rotation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .testTag("now_playing_screen")
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1A2B),
                            DarkBackground,
                            Color(0xFF09090D)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("now_playing_minimize")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            color = VipGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = currentSong.album,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    VipBadge(
                        vipStatus = vipStatus,
                        onClick = onOpenVipDialog
                    )
                }

                // View Mode Switcher Tabs (Vinyl, Visualizer, Lyrics)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ViewModeChip(
                        title = "Vinyl",
                        isSelected = viewMode == PlayerViewMode.VINYL,
                        onClick = { viewMode = PlayerViewMode.VINYL }
                    )
                    ViewModeChip(
                        title = "Visualizer",
                        isSelected = viewMode == PlayerViewMode.VISUALIZER,
                        onClick = { viewMode = PlayerViewMode.VISUALIZER }
                    )
                    ViewModeChip(
                        title = "Lyrics",
                        isSelected = viewMode == PlayerViewMode.LYRICS,
                        onClick = { viewMode = PlayerViewMode.LYRICS }
                    )
                }

                // Center Display based on ViewMode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (viewMode) {
                        PlayerViewMode.VINYL -> {
                            // Luxury Gold Vinyl Disc
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .rotate(if (isPlaying) rotationAngle else 0f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF2C2408),
                                                Color(0xFF14120D),
                                                Color(0xFF090805)
                                            )
                                        )
                                    )
                                    .border(4.dp, Color(0x66FFD700), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Concentric vinyl grooves
                                Box(
                                    modifier = Modifier
                                        .size(170.dp)
                                        .border(1.dp, Color(0x33FFD700), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .border(1.dp, Color(0x44FFD700), CircleShape)
                                )

                                // Center Label
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(VipGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        PlayerViewMode.VISUALIZER -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "3D Real-Time Spectrum Analyzer",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                AudioVisualizerView(
                                    visualizerState = visualizerState,
                                    isPlaying = isPlaying,
                                    height = 160.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Peak: ${(visualizerState.peakAmplitude * 100).toInt()}% • Bass: ${(visualizerState.bassIntensity * 100).toInt()}%",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        PlayerViewMode.LYRICS -> {
                            if (currentSong.lyrics.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.TextFields, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "لا توجد كلمات متاحة لهذا الملف",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = lyricsListState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(currentSong.lyrics) { index, lyric ->
                                        val isActive = index == activeLyricIndex
                                        Text(
                                            text = lyric.text,
                                            color = if (isActive) VipGold else TextSecondary.copy(alpha = 0.5f),
                                            fontSize = if (isActive) 17.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clickable { onSeek(lyric.timeMs) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Song Info & Format Badges
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentSong.title,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong.artist,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatBadge(text = currentSong.format, color = ElectricCyan)
                        FormatBadge(text = currentSong.bitrate, color = VipGold)
                        FormatBadge(text = currentSong.formattedSize, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Slider & Timers
                Column(modifier = Modifier.fillMaxWidth()) {
                    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

                    Slider(
                        value = progress,
                        onValueChange = { frac ->
                            onSeek((frac * durationMs).toLong())
                        },
                        modifier = Modifier.fillMaxWidth().testTag("now_playing_seek_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = VipGold,
                            activeTrackColor = VipGold,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = formatTime(durationMs),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Main Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.testTag("now_playing_shuffle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) VipGold else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Skip Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.testTag("now_playing_previous")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Big Play/Pause with Gold Ring
                    Box(
                        modifier = Modifier
                            .testTag("now_playing_play_pause")
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(VipGoldLight, VipGold)
                                )
                            )
                            .border(2.dp, Color(0xFFFFF0A0), CircleShape)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Skip Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.testTag("now_playing_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Repeat Mode
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier.testTag("now_playing_repeat")
                    ) {
                        val icon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        val tint = if (repeatMode != RepeatMode.OFF) VipGold else TextSecondary
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat",
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bottom Action Tools: Equalizer, Sleep Timer, Tag Editor, Favorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionIconButton(
                        icon = Icons.Default.Equalizer,
                        label = "Equalizer",
                        onClick = onOpenEqualizer,
                        tint = VipGold
                    )

                    ActionIconButton(
                        icon = Icons.Default.Bedtime,
                        label = "Sleep",
                        onClick = onOpenSleepTimer,
                        tint = ElectricCyan
                    )

                    ActionIconButton(
                        icon = Icons.Default.Edit,
                        label = "Tags",
                        onClick = { onOpenTagEditor(currentSong) },
                        tint = TextSecondary
                    )

                    ActionIconButton(
                        icon = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        label = "Favorite",
                        onClick = { onToggleFavorite(currentSong.id) },
                        tint = if (currentSong.isFavorite) Color(0xFFFF3B30) else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) VipGold else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun FormatBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

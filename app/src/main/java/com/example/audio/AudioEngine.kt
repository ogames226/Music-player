package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.model.LyricLine
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin

enum class RepeatMode {
    OFF, ALL, ONE
}

class AudioEngine(
    private val context: Context,
    private val equalizerEngine: EqualizerEngine
) {
    private val tag = "AudioEngine"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemainingMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingMinutes: StateFlow<Int?> = _sleepTimerRemainingMinutes.asStateFlow()

    private val _visualizerState = MutableStateFlow(VisualizerState())
    val visualizerState: StateFlow<VisualizerState> = _visualizerState.asStateFlow()

    private var onSongCompletionCallback: (() -> Unit)? = null
    private var tickCounter = 0L

    fun setOnCompletionListener(callback: () -> Unit) {
        onSongCompletionCallback = callback
    }

    fun playSong(song: Song) {
        stopCurrentPlayback()
        _currentSong.value = song
        _durationMs.value = song.durationMs
        _currentPositionMs.value = 0L

        if (song.path.isNotEmpty() && (song.path.startsWith("content://") || File(song.path).exists())) {
            playLocalFile(song)
        } else {
            playSyntheticTrack(song)
        }

        _isPlaying.value = true
        startProgressTracking()
    }

    private fun playLocalFile(song: Song) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (song.path.startsWith("content://")) {
                    setDataSource(context, Uri.parse(song.path))
                } else {
                    setDataSource(song.path)
                }
                prepare()
                equalizerEngine.bindAudioSession(audioSessionId)
                start()
                setOnCompletionListener {
                    handleTrackEnded()
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Local playback fallback to synth: ${e.message}")
            playSyntheticTrack(song)
        }
    }

    private fun playSyntheticTrack(song: Song) {
        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            equalizerEngine.bindAudioSession(track.audioSessionId)
            track.play()

            val scale = if (song.synthScale.isNotEmpty()) song.synthScale else listOf(440f)

            synthJob = scope.launch(Dispatchers.Default) {
                val bufferSize = 1024
                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                while (isActive && _isPlaying.value) {
                    val noteIndex = ((_currentPositionMs.value / 600) % scale.size).toInt()
                    val targetFreq = scale[noteIndex]
                    val bassFreq = scale[0] / 2f
                    val angularFreq = 2.0 * Math.PI * targetFreq / sampleRate
                    val bassAngularFreq = 2.0 * Math.PI * bassFreq / sampleRate

                    for (i in 0 until bufferSize) {
                        val sample1 = sin(phase) * 0.45
                        val sample2 = sin(phase * 0.5) * 0.35 // warm octave below
                        val bass = sin(phase * 0.25) * 0.25   // sub bass
                        val combined = ((sample1 + sample2 + bass) * 16000.0).toInt().coerceIn(-32767, 32767)
                        buffer[i] = combined.toShort()
                        phase += angularFreq
                        if (phase > 2.0 * Math.PI) {
                            phase -= 2.0 * Math.PI
                        }
                    }

                    track.write(buffer, 0, bufferSize)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "AudioTrack init simulated: ${e.message}")
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        try {
            mediaPlayer?.pause()
            audioTrack?.pause()
        } catch (e: Exception) {
            Log.w(tag, "Pause error: ${e.message}")
        }
    }

    fun resume() {
        if (_currentSong.value == null) return
        _isPlaying.value = true
        try {
            mediaPlayer?.start()
            audioTrack?.play()
        } catch (e: Exception) {
            Log.w(tag, "Resume error: ${e.message}")
        }
        if (synthJob == null || synthJob?.isActive == false) {
            _currentSong.value?.let { playSyntheticTrack(it) }
        }
        startProgressTracking()
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = clamped
        try {
            mediaPlayer?.seekTo(clamped.toInt())
        } catch (e: Exception) {
            // Ignore
        }
        updateActiveLyric(clamped)
    }

    fun toggleRepeatMode(): RepeatMode {
        val next = when (_repeatMode.value) {
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
            RepeatMode.OFF -> RepeatMode.ALL
        }
        _repeatMode.value = next
        return next
    }

    fun toggleShuffle(): Boolean {
        _isShuffle.value = !_isShuffle.value
        return _isShuffle.value
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        try {
            mediaPlayer?.let {
                it.playbackParams = it.playbackParams.setSpeed(speed)
            }
        } catch (e: Exception) {
            Log.w(tag, "Speed adjust: ${e.message}")
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingMinutes.value = null
            return
        }
        _sleepTimerRemainingMinutes.value = minutes

        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60_000L)
                remaining--
                _sleepTimerRemainingMinutes.value = remaining
            }
            if (isActive) {
                pause()
                _sleepTimerRemainingMinutes.value = null
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMinutes.value = null
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                delay(150L)
                tickCounter++

                if (mediaPlayer != null) {
                    try {
                        val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                        _currentPositionMs.value = pos
                    } catch (e: Exception) {
                        // Ignore
                    }
                } else {
                    val nextPos = _currentPositionMs.value + (150L * _playbackSpeed.value).toLong()
                    if (nextPos >= _durationMs.value && _durationMs.value > 0L) {
                        handleTrackEnded()
                        break
                    } else {
                        _currentPositionMs.value = nextPos
                    }
                }

                updateActiveLyric(_currentPositionMs.value)

                val bass = equalizerEngine.bassBoostLevel.value
                _visualizerState.value = VisualizerState.generateFrame(
                    isPlaying = _isPlaying.value,
                    timeTick = tickCounter,
                    bassBoost = bass
                )
            }
        }
    }

    private fun updateActiveLyric(currentPos: Long) {
        val lyrics = _currentSong.value?.lyrics ?: return
        if (lyrics.isEmpty()) {
            _activeLyricIndex.value = -1
            return
        }
        var foundIndex = -1
        for (i in lyrics.indices) {
            if (currentPos >= lyrics[i].timeMs) {
                foundIndex = i
            } else {
                break
            }
        }
        _activeLyricIndex.value = foundIndex
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0L)
                resume()
            }
            RepeatMode.ALL, RepeatMode.OFF -> {
                onSongCompletionCallback?.invoke()
            }
        }
    }

    fun stopCurrentPlayback() {
        progressJob?.cancel()
        synthJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
        }
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            audioTrack = null
        }
        _isPlaying.value = false
    }

    fun release() {
        stopCurrentPlayback()
        cancelSleepTimer()
    }
}

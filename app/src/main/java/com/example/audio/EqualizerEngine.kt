package com.example.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.EqualizerPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerEngine(private val context: Context) {

    private val tag = "EqualizerEngine"

    private var androidEqualizer: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var virtualizerFx: Virtualizer? = null

    private val _currentPreset = MutableStateFlow(EqualizerPreset.DEFAULT_PRESETS.first())
    val currentPreset: StateFlow<EqualizerPreset> = _currentPreset.asStateFlow()

    private val _bandLevels = MutableStateFlow(List(10) { 0f })
    val bandLevels: StateFlow<List<Float>> = _bandLevels.asStateFlow()

    private val _bassBoostLevel = MutableStateFlow(0f)
    val bassBoostLevel: StateFlow<Float> = _bassBoostLevel.asStateFlow()

    private val _virtualizerLevel = MutableStateFlow(0f)
    val virtualizerLevel: StateFlow<Float> = _virtualizerLevel.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            releaseEffects()

            val eq = Equalizer(0, audioSessionId)
            eq.enabled = _isEnabled.value
            androidEqualizer = eq

            val bb = BassBoost(0, audioSessionId)
            bb.enabled = _isEnabled.value
            bassBoostFx = bb

            val virt = Virtualizer(0, audioSessionId)
            virt.enabled = _isEnabled.value
            virtualizerFx = virt

            applyCurrentLevelsToHardware()
        } catch (e: Exception) {
            Log.w(tag, "AudioFx initialization bypassed or simulated: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        try {
            androidEqualizer?.enabled = enabled
            bassBoostFx?.enabled = enabled
            virtualizerFx?.enabled = enabled
        } catch (e: Exception) {
            Log.w(tag, "Failed to toggle AudioFx: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPreset.value = preset
        _bandLevels.value = preset.bandLevels
        _bassBoostLevel.value = preset.bassBoost
        _virtualizerLevel.value = preset.virtualizer
        applyCurrentLevelsToHardware()
    }

    fun setBandLevel(bandIndex: Int, levelDb: Float) {
        if (bandIndex in 0 until 10) {
            val updated = _bandLevels.value.toMutableList()
            updated[bandIndex] = levelDb.coerceIn(-12f, 12f)
            _bandLevels.value = updated
            applyCurrentLevelsToHardware()
        }
    }

    fun setBassBoost(level: Float) {
        val clamped = level.coerceIn(0f, 100f)
        _bassBoostLevel.value = clamped
        try {
            bassBoostFx?.let {
                if (it.strengthSupported) {
                    it.setStrength((clamped * 10).toInt().toShort()) // 0 to 1000
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error setting bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(level: Float) {
        val clamped = level.coerceIn(0f, 100f)
        _virtualizerLevel.value = clamped
        try {
            virtualizerFx?.let {
                if (it.strengthSupported) {
                    it.setStrength((clamped * 10).toInt().toShort()) // 0 to 1000
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Error setting virtualizer: ${e.message}")
        }
    }

    private fun applyCurrentLevelsToHardware() {
        try {
            val eq = androidEqualizer ?: return
            val numHardwareBands = eq.numberOfBands.toInt()
            val minDb = eq.bandLevelRange[0]
            val maxDb = eq.bandLevelRange[1]

            val current10 = _bandLevels.value
            for (i in 0 until numHardwareBands) {
                // Map the 10 UI bands to available hardware bands
                val mappedIndex = (i.toFloat() / (numHardwareBands - 1) * 9).toInt().coerceIn(0, 9)
                val targetDb = current10[mappedIndex]
                val scaled = ((targetDb + 12f) / 24f * (maxDb - minDb) + minDb).toInt().toShort()
                eq.setBandLevel(i.toShort(), scaled)
            }
        } catch (e: Exception) {
            Log.w(tag, "Error applying band levels to hardware: ${e.message}")
        }
    }

    fun releaseEffects() {
        try {
            androidEqualizer?.release()
            bassBoostFx?.release()
            virtualizerFx?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            androidEqualizer = null
            bassBoostFx = null
            virtualizerFx = null
        }
    }
}

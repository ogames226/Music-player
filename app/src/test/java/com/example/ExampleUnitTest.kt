package com.example

import com.example.model.EqualizerPreset
import com.example.model.Song
import com.example.model.SubscriptionPlan
import com.example.model.VipStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVipStatus_adFreeLogic() {
        val freeStatus = VipStatus(isVip = false, is24HourRewardActive = false)
        assertFalse(freeStatus.isAdFree)
        assertFalse(freeStatus.isProEqualizerUnlocked)

        val paidVipStatus = VipStatus(isVip = true, plan = SubscriptionPlan.YEARLY)
        assertTrue(paidVipStatus.isAdFree)
        assertTrue(paidVipStatus.isProEqualizerUnlocked)

        val reward24hStatus = VipStatus(
            isVip = false,
            is24HourRewardActive = true,
            expiresAtMs = System.currentTimeMillis() + 3600_000L * 12
        )
        assertTrue(reward24hStatus.isAdFree)
        assertTrue(reward24hStatus.isProEqualizerUnlocked)
        assertTrue(reward24hStatus.remainingRewardHours in 11..12)
    }

    @Test
    fun testSongFormattedDuration() {
        val song = Song(
            id = "test_1",
            title = "Test Track",
            artist = "Artist",
            album = "Album",
            durationMs = 214_000L
        )
        assertEquals("03:34", song.formattedDuration)
    }

    @Test
    fun testEqualizerPresetsCount() {
        val presets = EqualizerPreset.DEFAULT_PRESETS
        assertTrue(presets.isNotEmpty())
        assertEquals(10, EqualizerPreset.BAND_LABELS.size)
        presets.forEach { preset ->
            assertEquals(10, preset.bandLevels.size)
        }
    }
}

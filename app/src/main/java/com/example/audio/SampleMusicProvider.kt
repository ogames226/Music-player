package com.example.audio

import com.example.model.LyricLine
import com.example.model.Song

object SampleMusicProvider {

    fun getSampleSongs(): List<Song> {
        return listOf(
            Song(
                id = "song_1",
                title = "Desert Nights (ليالي الصحراء)",
                artist = "Sultan Al-Aoud & Ensemble",
                album = "Arabian Horizons VIP",
                durationMs = 214_000L,
                genre = "Oriental Fusion",
                bitrate = "FLAC 24-bit 96kHz",
                format = "FLAC",
                fileSizeBytes = 28_400_000L,
                isFavorite = true,
                isSynthTrack = true,
                synthScale = listOf(220f, 246.94f, 277.18f, 329.63f, 369.99f, 440f),
                lyrics = listOf(
                    LyricLine(0L, "♪ (موسيقى عود شرقية هادئة وألحان ناي ساحرة) ♪"),
                    LyricLine(12_000L, "في هدوء الليل وسكون الصحراء"),
                    LyricLine(24_000L, "تعزف الأوتار ألحان الوفاء"),
                    LyricLine(36_000L, "نسيمٌ عليلٌ يُداعبُ الرمال الذهبية"),
                    LyricLine(50_000L, "ويتردد صدى النغمات في الأرجاء العلية"),
                    LyricLine(68_000L, "♪ (صولو عود مع إيقاع دفوف خليجية دافئة) ♪"),
                    LyricLine(88_000L, "يا ليل طال بي السهر مع الأحباب"),
                    LyricLine(104_000L, "نفتح مع الموسيقى أجمل الأبواب"),
                    LyricLine(125_000L, "صوتك وتر ينبض في الأعماق"),
                    LyricLine(148_000L, "يُشعل في القلوب شوقاً واشتياق"),
                    LyricLine(175_000L, "♪ (نهاية اللحن بنغمات بيانو وأوتار هادئة) ♪")
                )
            ),
            Song(
                id = "song_2",
                title = "Golden Royalty VIP",
                artist = "The Maestro Orchestra",
                album = "Music Master Classics Vol. 1",
                durationMs = 195_000L,
                genre = "Classical / Cinematic",
                bitrate = "320 kbps High Quality",
                format = "MP3",
                fileSizeBytes = 7_800_000L,
                isFavorite = true,
                isSynthTrack = true,
                synthScale = listOf(261.63f, 329.63f, 392.00f, 523.25f, 659.25f),
                lyrics = listOf(
                    LyricLine(0L, "♪ (Grand orchestral brass and violin intro) ♪"),
                    LyricLine(15_000L, "Welcome to the golden hall of sound"),
                    LyricLine(28_000L, "Where kings and queens of rhythm are crowned"),
                    LyricLine(42_000L, "Harmonies rising high above the ground"),
                    LyricLine(58_000L, "Pure acoustic brilliance all around"),
                    LyricLine(75_000L, "♪ (Dynamic crescendo with thunderous timpani) ♪"),
                    LyricLine(95_000L, "Feel the bass resonating in your soul"),
                    LyricLine(115_000L, "Music Master takes complete control"),
                    LyricLine(135_000L, "VIP clarity, perfection in every chord"),
                    LyricLine(160_000L, "♪ (Triumphant brass fanfare finale) ♪")
                )
            ),
            Song(
                id = "song_3",
                title = "Cyberpulse 2088",
                artist = "Neon Matrix",
                album = "Synthwave VIP Edition",
                durationMs = 242_000L,
                genre = "Synthwave / EDM",
                bitrate = "FLAC 24-bit 192kHz",
                format = "FLAC",
                fileSizeBytes = 45_100_000L,
                isFavorite = false,
                isSynthTrack = true,
                synthScale = listOf(130.81f, 164.81f, 196.00f, 261.63f, 392.00f),
                lyrics = listOf(
                    LyricLine(0L, "⚡ (Analog synth arpeggio pulse) ⚡"),
                    LyricLine(18_000L, "Cruising down the neon grid at midnight"),
                    LyricLine(35_000L, "Subwoofer rumbling under violet light"),
                    LyricLine(52_000L, "Digital frequencies igniting the dark"),
                    LyricLine(70_000L, "⚡ (Heavy 808 Bass Drop - Feel the Sub) ⚡"),
                    LyricLine(92_000L, "3D surround sound swirling in the sphere"),
                    LyricLine(112_000L, "Crystal holographic notes so pure and clear"),
                    LyricLine(135_000L, "Never stop the tempo, let the wave collide"),
                    LyricLine(160_000L, "With Music Master VIP on every ride")
                )
            ),
            Song(
                id = "song_4",
                title = "Midnight Jazz Lounge",
                artist = "Oliver Brooks Quartet",
                album = "Velvet Saxophone",
                durationMs = 180_000L,
                genre = "Smooth Jazz",
                bitrate = "320 kbps High Quality",
                format = "MP3",
                fileSizeBytes = 7_200_000L,
                isFavorite = true,
                isSynthTrack = true,
                synthScale = listOf(174.61f, 220.00f, 261.63f, 329.63f, 392.00f),
                lyrics = listOf(
                    LyricLine(0L, "🎷 (Velvet saxophone solo with warm double bass) 🎷"),
                    LyricLine(14_000L, "Rain tapping softly on the window pane"),
                    LyricLine(30_000L, "A warm cup of coffee, washing out the pain"),
                    LyricLine(46_000L, "Brushes on the snare, taking it so slow"),
                    LyricLine(64_000L, "Saxophone whispering memories we know"),
                    LyricLine(85_000L, "🎷 (Intricate Rhodes piano improvisation) 🎷"),
                    LyricLine(110_000L, "Unwind your mind, let the worries fade"),
                    LyricLine(135_000L, "In the peaceful sanctuary jazz has made"),
                    LyricLine(160_000L, "♪ (Gentle fading brush rhythm) ♪")
                )
            ),
            Song(
                id = "song_5",
                title = "Acoustic Sunset (غروب شاعري)",
                artist = "Nour & The Strings",
                album = "Unplugged Sessions",
                durationMs = 205_000L,
                genre = "Acoustic / Folk",
                bitrate = "WAV 16-bit 44.1kHz",
                format = "WAV",
                fileSizeBytes = 36_000_000L,
                isFavorite = false,
                isSynthTrack = true,
                synthScale = listOf(196.00f, 246.94f, 293.66f, 392.00f, 440.00f),
                lyrics = listOf(
                    LyricLine(0L, "🎸 (أنغام جيتار كلاسيكي دافئ) 🎸"),
                    LyricLine(16_000L, "تغيب الشمس وراء الأفق البعيد"),
                    LyricLine(32_000L, "ويولد في الروح أملٌ جديد"),
                    LyricLine(50_000L, "مع كل وتر وكل همسة سلام"),
                    LyricLine(68_000L, "تتراقص النجوم وتغفو الأنام"),
                    LyricLine(90_000L, "🎸 (تناغم بيانو وجيتار شاعري) 🎸"),
                    LyricLine(120_000L, "صفاء الصوت ونقاء الإحساس"),
                    LyricLine(145_000L, "كنزٌ ثمينٌ يُهدى لأغلى الناس")
                )
            )
        )
    }
}

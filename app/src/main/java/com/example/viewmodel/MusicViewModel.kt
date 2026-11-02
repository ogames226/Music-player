package com.example.viewmodel

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.EqualizerEngine
import com.example.audio.RepeatMode
import com.example.audio.SampleMusicProvider
import com.example.audio.VisualizerState
import com.example.billing.AdMobManager
import com.example.billing.BillingManager
import com.example.model.EqualizerPreset
import com.example.model.Playlist
import com.example.model.Song
import com.example.model.SubscriptionPlan
import com.example.model.VipStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab(val titleAr: String, val titleEn: String) {
    SONGS("الأغاني", "Songs"),
    ALBUMS("الألبومات", "Albums"),
    ARTISTS("الفنانون", "Artists"),
    PLAYLISTS("القوائم", "Playlists"),
    FAVORITES("المفضلة", "Favorites"),
    FOLDERS("المجلدات", "Folders")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    val equalizerEngine = EqualizerEngine(context)
    val audioEngine = AudioEngine(context, equalizerEngine)
    val billingManager = BillingManager(context)
    val adMobManager = AdMobManager(context)

    // Playback state delegated from AudioEngine
    val currentSong: StateFlow<Song?> = audioEngine.currentSong
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs
    val activeLyricIndex: StateFlow<Int> = audioEngine.activeLyricIndex
    val repeatMode: StateFlow<RepeatMode> = audioEngine.repeatMode
    val isShuffle: StateFlow<Boolean> = audioEngine.isShuffle
    val playbackSpeed: StateFlow<Float> = audioEngine.playbackSpeed
    val sleepTimerMinutes: StateFlow<Int?> = audioEngine.sleepTimerRemainingMinutes
    val visualizerState: StateFlow<VisualizerState> = audioEngine.visualizerState

    // Equalizer state delegated from EqualizerEngine
    val currentPreset: StateFlow<EqualizerPreset> = equalizerEngine.currentPreset
    val bandLevels: StateFlow<List<Float>> = equalizerEngine.bandLevels
    val bassBoost: StateFlow<Float> = equalizerEngine.bassBoostLevel
    val virtualizer: StateFlow<Float> = equalizerEngine.virtualizerLevel
    val isEqEnabled: StateFlow<Boolean> = equalizerEngine.isEnabled

    // Billing & Ads
    val vipStatus: StateFlow<VipStatus> = billingManager.vipStatus
    val isShowingInterstitial: StateFlow<Boolean> = adMobManager.isShowingInterstitial
    val isShowingRewarded: StateFlow<Boolean> = adMobManager.isShowingRewarded

    // Library & Navigation state
    private val _songs = MutableStateFlow(SampleMusicProvider.getSampleSongs())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _playlists = MutableStateFlow(
        listOf(
            Playlist("pl_1", "VIP Master Favorites", listOf("song_1", "song_2")),
            Playlist("pl_2", "Midnight Relaxation", listOf("song_3", "song_4", "song_5"))
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog & Screen Visibility
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _showEqualizerDialog = MutableStateFlow(false)
    val showEqualizerDialog: StateFlow<Boolean> = _showEqualizerDialog.asStateFlow()

    private val _showVipDialog = MutableStateFlow(false)
    val showVipDialog: StateFlow<Boolean> = _showVipDialog.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private val _showTagEditorDialog = MutableStateFlow(false)
    val showTagEditorDialog: StateFlow<Boolean> = _showTagEditorDialog.asStateFlow()

    private val _editingSong = MutableStateFlow<Song?>(null)
    val editingSong: StateFlow<Song?> = _editingSong.asStateFlow()

    // Filtered songs
    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery, _selectedTab) { list, query, tab ->
        val queryTrimmed = query.trim().lowercase()
        val baseList = when (tab) {
            LibraryTab.FAVORITES -> list.filter { it.isFavorite }
            else -> list
        }
        if (queryTrimmed.isEmpty()) {
            baseList
        } else {
            baseList.filter {
                it.title.lowercase().contains(queryTrimmed) ||
                it.artist.lowercase().contains(queryTrimmed) ||
                it.album.lowercase().contains(queryTrimmed) ||
                it.genre.lowercase().contains(queryTrimmed)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, _songs.value)

    init {
        audioEngine.setOnCompletionListener {
            playNext(auto = true)
        }
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song) {
        adMobManager.onSongChanged(vipStatus.value) {
            audioEngine.playSong(song)
        }
    }

    fun togglePlayPause() {
        if (currentSong.value == null && _songs.value.isNotEmpty()) {
            playSong(_songs.value.first())
        } else {
            audioEngine.togglePlayPause()
        }
    }

    fun playNext(auto: Boolean = false) {
        val current = currentSong.value ?: return
        val list = filteredSongs.value.ifEmpty { _songs.value }
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextIndex = if (isShuffle.value) {
            (list.indices - currentIndex).randomOrNull() ?: 0
        } else {
            (currentIndex + 1) % list.size
        }

        val nextSong = list.getOrNull(nextIndex) ?: list.first()
        if (auto) {
            audioEngine.playSong(nextSong)
        } else {
            adMobManager.onSongChanged(vipStatus.value) {
                audioEngine.playSong(nextSong)
            }
        }
    }

    fun playPrevious() {
        val current = currentSong.value ?: return
        val list = filteredSongs.value.ifEmpty { _songs.value }
        if (list.isEmpty()) return

        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        val prevSong = list.getOrNull(prevIndex) ?: list.first()

        adMobManager.onSongChanged(vipStatus.value) {
            audioEngine.playSong(prevSong)
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun toggleRepeat() {
        audioEngine.toggleRepeatMode()
    }

    fun toggleShuffle() {
        audioEngine.toggleShuffle()
    }

    fun setPlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
    }

    fun toggleFavorite(songId: String) {
        _songs.value = _songs.value.map {
            if (it.id == songId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun openNowPlaying() {
        _isNowPlayingExpanded.value = true
    }

    fun closeNowPlaying() {
        _isNowPlayingExpanded.value = false
    }

    fun openEqualizer() {
        _showEqualizerDialog.value = true
    }

    fun closeEqualizer() {
        _showEqualizerDialog.value = false
    }

    fun openVipDialog() {
        _showVipDialog.value = true
    }

    fun closeVipDialog() {
        _showVipDialog.value = false
    }

    fun openSleepTimerDialog() {
        _showSleepTimerDialog.value = true
    }

    fun closeSleepTimerDialog() {
        _showSleepTimerDialog.value = false
    }

    fun openTagEditor(song: Song) {
        _editingSong.value = song
        _showTagEditorDialog.value = true
    }

    fun closeTagEditor() {
        _showTagEditorDialog.value = false
        _editingSong.value = null
    }

    fun saveSongTags(songId: String, title: String, artist: String, album: String, genre: String) {
        _songs.value = _songs.value.map {
            if (it.id == songId) {
                it.copy(title = title, artist = artist, album = album, genre = genre)
            } else {
                it
            }
        }
        if (currentSong.value?.id == songId) {
            _songs.value.find { it.id == songId }?.let {
                // Keep playing updated song metadata
            }
        }
        closeTagEditor()
    }

    // Equalizer controls
    fun setEqualizerBand(index: Int, levelDb: Float) {
        equalizerEngine.setBandLevel(index, levelDb)
    }

    fun setBassBoost(level: Float) {
        equalizerEngine.setBassBoost(level)
    }

    fun setVirtualizer(level: Float) {
        equalizerEngine.setVirtualizer(level)
    }

    fun applyPreset(preset: EqualizerPreset) {
        if (preset.isVipExclusive && !vipStatus.value.isProEqualizerUnlocked) {
            openVipDialog()
            return
        }
        equalizerEngine.applyPreset(preset)
    }

    fun toggleEqualizer(enabled: Boolean) {
        equalizerEngine.setEnabled(enabled)
    }

    // Sleep timer
    fun setSleepTimer(minutes: Int) {
        audioEngine.startSleepTimer(minutes)
        closeSleepTimerDialog()
    }

    fun cancelSleepTimer() {
        audioEngine.cancelSleepTimer()
        closeSleepTimerDialog()
    }

    // VIP Subscription & Ads
    fun subscribe(plan: SubscriptionPlan, gateway: String) {
        billingManager.processSubscription(
            plan = plan,
            gatewayName = gateway,
            onSuccess = {
                closeVipDialog()
            },
            onError = {
                // Handled
            }
        )
    }

    fun watchRewardedAdFor24hVip(activity: android.app.Activity? = null) {
        adMobManager.showRewardedAd(activity) {
            billingManager.activate24HourRewardPass()
            closeVipDialog()
        }
    }

    fun completeRewardedAd() {
        adMobManager.completeRewardedAd {
            billingManager.activate24HourRewardPass()
        }
    }

    fun dismissRewardedAd() {
        adMobManager.dismissRewardedAd()
    }

    fun dismissInterstitial() {
        adMobManager.dismissInterstitial {
            // Dismissed
        }
    }

    fun toggleTestVip() {
        billingManager.toggleVipForTesting()
    }

    /**
     * Scans local audio files on the device via MediaStore and adds them to library
     */
    fun scanDeviceAudio() {
        viewModelScope.launch {
            try {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)

                val scanned = mutableListOf<Song>()
                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val title = it.getString(titleCol) ?: "Unknown Track"
                        val artist = it.getString(artistCol) ?: "Unknown Artist"
                        val album = it.getString(albumCol) ?: "Unknown Album"
                        val duration = it.getLong(durationCol)
                        val data = it.getString(dataCol) ?: ""
                        val size = it.getLong(sizeCol)

                        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                        scanned.add(
                            Song(
                                id = "local_$id",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = if (duration > 0) duration else 180_000L,
                                path = contentUri.toString(),
                                genre = "Local Device Audio",
                                bitrate = "HQ Audio",
                                format = if (data.endsWith(".flac", true)) "FLAC" else "MP3",
                                fileSizeBytes = size,
                                isFavorite = false,
                                isSynthTrack = false
                            )
                        )
                    }
                }
                if (scanned.isNotEmpty()) {
                    _songs.value = scanned + _songs.value.filter { it.isSynthTrack }
                }
            } catch (e: Exception) {
                Log.w("MusicViewModel", "Device audio scan: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        equalizerEngine.releaseEffects()
    }
}

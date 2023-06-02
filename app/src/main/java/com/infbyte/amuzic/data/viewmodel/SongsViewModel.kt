package com.infbyte.amuzic.data.viewmodel

import android.media.AudioManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infbyte.amuzic.data.model.Album
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.data.model.Folder
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.data.model.SongsRepo
import com.infbyte.amuzic.playback.PlaybackListener
import com.infbyte.amuzic.playback.PlaybackManager
import com.infbyte.amuzic.playback.PlaybackModes.REPEAT_ALL
import com.infbyte.amuzic.ui.screens.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor() : ViewModel() {

    @Inject lateinit var songsRepo: SongsRepo

    @Inject lateinit var player: PlaybackListener

    private lateinit var playbackManager: PlaybackManager

    private val _isLoadingSongs = mutableStateOf(false)
    val isLoadingSongs: State<Boolean> = _isLoadingSongs
    private val _isLoaded = mutableStateOf(false)
    val isLoaded: State<Boolean> = _isLoaded

    private var hasAudioFocus = false
    val audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    onPauseSong()
                }
            }
        }

    var confirmExit = false
        private set

    private val _currentScreen = mutableStateOf(Screens.ALL_SONGS)
    val currentScreen: State<String> = _currentScreen
    val showPopup = mutableStateOf(false)
    private val _showTopBar = mutableStateOf(false)
    val showTopBar: State<Boolean> = _showTopBar
    private val _showPlayBar = mutableStateOf(false)
    val showPlayBar: State<Boolean> = _showPlayBar
    private var scrollValue = 0
    var songs = listOf<Song>()
        private set
    val artists
        get() = songsRepo.artists
    val albums
        get() = songsRepo.albums
    val folders
        get() = songsRepo.folders
    private val _showSongs = mutableStateOf(true)
    val showSongs: State<Boolean> = _showSongs
    private val _showArtists = mutableStateOf(false)
    val showArtists = _showArtists
    private val _showAlbums = mutableStateOf(false)
    val showAlbums = _showAlbums
    private val _showFolders = mutableStateOf(false)
    val showFolders = _showFolders
    private val screenStates = listOf(
        _showSongs,
        _showArtists,
        _showAlbums,
        _showFolders
    )

    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> = _currentSong
    private val _currentArtist = mutableStateOf<Artist?>(null)
    val currentArtist: State<Artist?> = _currentArtist
    private val _currentAlbum = mutableStateOf<Album?>(null)
    val currentAlbum: State<Album?> = _currentAlbum
    private val _currentFolder = mutableStateOf<Folder?>(null)
    val currentFolder: State<Folder?> = _currentFolder
    val mode = mutableStateOf(REPEAT_ALL)
    val progress = mutableStateOf(0f)
    val isPlaying = mutableStateOf(false)

    fun setPlaybackManager(pManager: PlaybackManager) {
        playbackManager = pManager
    }

    fun confirmExit() {
        viewModelScope.launch {
            confirmExit = true
            delay(2000)
            confirmExit = false
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            songsRepo.loadSongs(
                {
                    _isLoadingSongs.value = true
                    player.init(
                        songsRepo.appContext,
                        { onPlaybackPrepared() },
                        { onPlaybackCompleted() }
                    )
                },
                {
                    _isLoadingSongs.value = false
                    _isLoaded.value = true
                    songs = songsRepo.songs
                    _currentSong.value = songs.first()
                    _currentSong.value?.let {
                        player.initSong(it)
                    }
                }
            )
        }
    }

    fun onSongClicked(song: Song) {
        _currentSong.value = song
        showAndDelayHidePlayBar()
        player.prepareSong(song)
    }

    fun onPlayClicked() {
        if (player.isActive()) {
            onPauseSong()
            return
        }
        onPlaySong()
    }

    fun onNextClicked() {
        nextSong()
    }

    fun onPrevClicked() {
        prevSong()
    }

    fun onArtistClicked(pos: Int) {
        _currentArtist.value = artists[pos]
        songs = currentArtist.value?.let { artist ->
            songsRepo.songs.filter { song ->
                song.artist == artist.name
            }
        }!!
        showSongs()
    }

    fun onAlbumClicked(pos: Int) {
        _currentAlbum.value = albums[pos]
        songs = currentAlbum.value?.let { album ->
            songsRepo.songs.filter { song ->
                song.album == album.name
            }
        }!!
        showSongs()
    }

    fun onFolderClicked(pos: Int) {
        _currentFolder.value = folders[pos]
        songs = currentFolder.value?.let { folder ->
            songsRepo.songs.filter { song ->
                song.folder == folder.name
            }
        }!!
        showSongs()
    }

    fun onScreenSelected(screen: String) {
        _currentScreen.value = screen
        if (screen == Screens.ALL_SONGS) {
            onAllSongsClicked()
        }
        toggleAnimateScreen()
    }

    fun onTogglePopup() {
        showPopup.value = !showPopup.value
    }

    fun showAndDelayHideTopBar() {
        switchOnDelayOffBoolState(_showTopBar)
    }

    fun showTopBar() {
        switchOnBoolState(_showTopBar)
    }

    fun hideTopBar() {
        switchOffBoolState(_showTopBar)
    }

    fun showAndDelayHidePlayBar() {
        switchOnDelayOffBoolState(_showPlayBar)
    }

    fun showPlayBar() {
        switchOnBoolState(_showPlayBar)
    }

    fun hidePlayBar() {
        switchOffBoolState(_showPlayBar)
    }

    fun toggleBarsByScroll(
        value: Int
    ) {
        if (value != 0) {
            calcScrollDelta(value) { delta ->
                toggleTopBarByScrollDelta(delta)
                togglePlayBarByScrollDelta(delta)
            }
        } else {
            showAndDelayHideTopBar()
            showAndDelayHidePlayBar()
        }
    }

    private fun onAllSongsClicked() {
        songs = songsRepo.songs
    }

    private fun togglePlayBarByScrollDelta(delta: Int) {
        if (delta > 0) {
            _showPlayBar.value = true
            return
        }
        if (delta < 0) {
            _showPlayBar.value = false
        }
    }

    private fun togglePlayBar() {
        _showPlayBar.value = !showPlayBar.value
    }

    private fun toggleTopBarByScrollDelta(delta: Int) {
        if (delta < 0) {
            _showTopBar.value = true
            return
        }
        if (delta > 0) {
            _showTopBar.value = false
        }
    }

    private fun toggleTopBar() {
        _showTopBar.value = !showTopBar.value
    }

    private fun calcScrollDelta(
        scrollValue: Int,
        toggle: (Int) -> Unit
    ) {
        val delta = this@SongsViewModel.scrollValue - scrollValue
        this@SongsViewModel.scrollValue = scrollValue
        toggle(delta)
    }

    private fun switchOnDelayOffBoolState(boolState: MutableState<Boolean>) {
        if (!boolState.value) {
            viewModelScope.launch {
                boolState.value = true
                delay(5000)
                boolState.value = false
            }
        }
    }

    private fun switchOffBoolState(boolState: MutableState<Boolean>) {
        boolState.value = false
    }

    private fun switchOnBoolState(boolState: MutableState<Boolean>) {
        boolState.value = true
    }

    private fun toggleAnimateScreen() {
        when (_currentScreen.value) {
            Screens.ALL_SONGS -> filterStatesToggle(
                _showSongs
            )
            Screens.ARTISTS -> filterStatesToggle(
                _showArtists
            )
            Screens.ALBUMS -> filterStatesToggle(
                _showAlbums
            )
            Screens.FOLDERS -> filterStatesToggle(
                _showFolders
            )
        }
    }

    private fun filterStatesToggle(
        state: MutableState<Boolean>
    ) {
        viewModelScope.launch {
            delay(500)
            state.value = true
            screenStates.filter {
                it != state
            }.forEach {
                it.value = false
            }
        }
    }

    private fun showSongs() {
        filterStatesToggle(_showSongs)
    }

    private fun startProgressMonitor() {
        viewModelScope.launch(Dispatchers.Default) {
            do {
                progress.value = calcProgress()
            } while (player.isActive())
        }
    }

    private fun calcProgress() =
        player.progress().toFloat() / player.duration().toFloat()

    private fun onPlaySong() {
        playbackManager.requestAudioFocus { isGranted ->
            hasAudioFocus = isGranted
            if (hasAudioFocus) {
                player.playSong()
                isPlaying.value = player.isActive()
                startProgressMonitor()
            }
        }
    }

    private fun onPauseSong() {
        player.pauseSong()
        playbackManager.abandonAudioFocus()
        hasAudioFocus = false
        isPlaying.value = player.isActive()
    }

    private fun nextSong() {
        val currentPos = songs.indexOf(_currentSong.value)
        val nextPos = currentPos + 1
        if (nextPos < songs.size) {
            onSongClicked(songs[nextPos])
        }
        if (nextPos == songs.size) {
            onSongClicked(songs.first())
        }
    }

    private fun prevSong() {
        val currentPos = songs.indexOf(_currentSong.value)
        val prevPos = currentPos - 1
        if (prevPos >= 0) {
            onSongClicked(songs[prevPos])
        }
        if (prevPos < 0) {
            onSongClicked(songs.last())
        }
    }

    private fun onPlaybackPrepared() {
        onPlaySong()
    }

    private fun onPlaybackCompleted() {
        when (mode.value) {
            REPEAT_ALL -> {
                onNextClicked()
            }
        }
    }
}

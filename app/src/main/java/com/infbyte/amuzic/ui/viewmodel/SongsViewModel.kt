package com.infbyte.amuzic.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infbyte.amuzic.data.model.Album
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.data.model.Folder
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.data.model.SongsRepo
import com.infbyte.amuzic.playback.PlaybackListener
import com.infbyte.amuzic.playback.PlaybackManager
import com.infbyte.amuzic.playback.PlaybackMode
import com.infbyte.amuzic.playback.PlaybackMode.REPEAT_ALL
import com.infbyte.amuzic.playback.PlaybackMode.REPEAT_ONE
import com.infbyte.amuzic.playback.PlaybackMode.SHUFFLE
import com.infbyte.amuzic.ui.screens.Screens
import com.infbyte.amuzic.ui.viewmodel.SongsState.Companion.INITIAL_STATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongsState(
    val currentSong: Song = Song(0, "", "", "", "", Uri.EMPTY, null),
    val songs: List<Song> = listOf(),
    val searchResult: List<Song> = listOf(),
    val icon: ImageBitmap? = null
) {
    companion object {
        val INITIAL_STATE = SongsState()
    }
}

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val repo: SongsRepo,
    private val playbackListener: PlaybackListener,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    var state by mutableStateOf(INITIAL_STATE)
        private set

    private val _isLoadingSongs = mutableStateOf(false)
    val isLoadingSongs: State<Boolean> = _isLoadingSongs
    private val _isLoaded = mutableStateOf(false)
    val isLoaded: State<Boolean> = _isLoaded

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
        get() = this.repo.artists
    val albums
        get() = this.repo.albums
    val folders
        get() = this.repo.folders
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
    private val _playbackMode = mutableStateOf(REPEAT_ALL)
    val playbackMode: State<PlaybackMode> = _playbackMode
    val progress = mutableStateOf(0f)
    val songDuration = mutableStateOf(0)

    fun init(context: Context) {
        viewModelScope.launch {
            playbackManager.setPlaybackListener(playbackListener)
            loadSongs(context)
        }
    }

    fun isPlaying(): State<Boolean> = playbackManager.isPlaying

    fun confirmExit() {
        viewModelScope.launch {
            confirmExit = true
            delay(2000)
            confirmExit = false
        }
    }

    fun onSongClicked(song: Song) {
        if (currentSong.value == song && !playbackListener.isActive()) {
            onPlaySong()
            return
        }
        if (currentSong.value == song && playbackListener.isActive()) {
            return
        }
        _currentSong.value = song
        showAndDelayHidePlayBar()
        playbackListener.prepareSong(song)
    }

    fun onPlayClicked() {
        if (playbackListener.isActive()) {
            onPauseSong()
            return
        }
        onPlaySong()
    }

    fun onNextClicked() {
        when (playbackMode.value) {
            REPEAT_ONE -> {
                nextSong()
            }
            REPEAT_ALL -> {
                nextSong()
            }
            SHUFFLE -> {
                shuffle()
            }
        }
    }

    fun onPrevClicked() {
        when (playbackMode.value) {
            REPEAT_ONE -> {
                prevSong()
            }
            REPEAT_ALL -> {
                prevSong()
            }
            SHUFFLE -> {
                shuffle()
            }
        }
    }

    fun onArtistClicked(artist: Artist) {
        viewModelScope.launch {
            _currentArtist.value = artist
            songs = currentArtist.value?.let { artist1 ->
                repo.songs.filter { song ->
                    song.artist == artist1.name
                }
            }!!
            val icon = songs.map { it.thumbnail }.filterNotNull().firstOrNull()?.asImageBitmap()
            state = state.copy(songs = songs, icon = icon)
        }
    }

    fun onAlbumClicked(album: Album) {
        viewModelScope.launch {
            _currentAlbum.value = album
            songs = currentAlbum.value?.let { album1 ->
                repo.songs.filter { song ->
                    song.album == album1.name
                }
            }!!
            val icon = songs.map { it.thumbnail }.filterNotNull().firstOrNull()?.asImageBitmap()
            state = state.copy(songs = songs, icon = icon)
        }
    }

    fun onFolderClicked(pos: Int) {
        _currentFolder.value = folders[pos]
        songs = currentFolder.value?.let { folder ->
            repo.songs.filter { song ->
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

    fun hideBars() {
        hideTopBar()
        hidePlayBar()
    }

    fun togglePlayBarByScroll(
        value: Int
    ) {
        if (value != 0) {
            calcScrollDelta(value) { delta ->
                togglePlayBarByScrollDelta(delta)
            }
        } else {
            showAndDelayHidePlayBar()
        }
    }

    fun onTogglePlaybackMode() {
        _playbackMode.value = when (playbackMode.value) {
            REPEAT_ONE -> REPEAT_ALL
            REPEAT_ALL -> SHUFFLE
            SHUFFLE -> REPEAT_ONE
        }
    }

    private fun onAllSongsClicked() {
        songs = this.repo.songs
    }

    private fun togglePlayBarByScrollDelta(delta: Int) {
        if (delta > 0) {
            showAndDelayHidePlayBar()
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
        val delta = this.scrollValue - scrollValue
        this.scrollValue = scrollValue
        toggle(delta)
    }

    private fun switchOnDelayOffBoolState(boolState: MutableState<Boolean>) {
        if (!boolState.value) {
            viewModelScope.launch {
                boolState.value = true
                delay(7000)
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
            } while (playbackListener.isActive())
        }
    }

    private fun calcProgress() =
        playbackListener.progress().toFloat() / playbackListener.duration().toFloat()

    private fun onPlaySong() {
        playbackManager.requestAudioFocus { isGranted ->
            if (isGranted) {
                songDuration.value = playbackListener.duration()
                playbackListener.playSong()
                playbackManager.checkPlayer()
                startProgressMonitor()
            }
        }
    }

    private fun onPauseSong() {
        playbackManager.pauseSong()
    }

    private fun nextSong() {
        val currentPos = songs.indexOf(currentSong.value)
        val nextPos = currentPos + 1
        if (nextPos < songs.size) {
            onSongClicked(songs[nextPos])
        }
        if (nextPos == songs.size) {
            onSongClicked(songs.first())
        }
    }

    private fun prevSong() {
        val currentPos = songs.indexOf(currentSong.value)
        val prevPos = currentPos - 1
        if (prevPos >= 0) {
            onSongClicked(songs[prevPos])
        }
        if (prevPos < 0) {
            onSongClicked(songs.last())
        }
    }

    private fun shuffle() {
        val song = songs.filter { it != currentSong.value }.random()
        onSongClicked(song)
    }

    private fun onPlaybackPrepared() {
        onPlaySong()
    }

    private fun onPlaybackCompleted() {
        when (playbackMode.value) {
            REPEAT_ONE -> { onPlaySong() }
            else -> {
                nextSong()
            }
        }
    }

    private fun loadSongs(context: Context) {
        viewModelScope.launch {
            repo.loadSongs(
                {
                    _isLoadingSongs.value = true
                    playbackListener.init(
                        context,
                        { onPlaybackPrepared() },
                        { onPlaybackCompleted() }
                    )
                },
                { songs ->
                    _isLoadingSongs.value = false
                    _isLoaded.value = true
                    this@SongsViewModel.songs = songs
                    state = state.copy(currentSong = songs.first(), songs = songs, searchResult = songs)
                    _currentSong.value = songs.first()
                    currentSong.value?.let { song ->
                        playbackListener.initSong(song)
                    }
                }
            )
        }
    }

    fun onSeekTouch(position: Float) {
        playbackListener.seekTo(position)
    }

    fun onSearch(query: String) {
        viewModelScope.launch {
            state = with(state) {
                copy(searchResult = songs.filter { song -> song.name.contains(query, true) })
            }
        }
    }

    fun onExit() {
        playbackListener.stopSong()
        playbackListener.releasePlayer()
    }
}

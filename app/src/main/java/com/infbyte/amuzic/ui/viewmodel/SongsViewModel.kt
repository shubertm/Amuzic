package com.infbyte.amuzic.ui.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.Player.RepeatMode
import com.infbyte.amuzic.data.model.Album
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.data.repo.SongsRepo
import com.infbyte.amuzic.playback.AmuzicPlayer
import com.infbyte.amuzic.ui.viewmodel.AmuzicState.Companion.INITIAL_STATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AmuzicState(
    val currentSong: Song = Song(),
    val currentArtist: Artist = Artist(),
    val currentAlbum: Album = Album(),
    val songs: List<Song> = listOf(),
    val currentPlaylist: List<Song> = listOf(),
    val artists: List<Artist> = listOf(),
    val albums: List<Album> = listOf(),
    val songsSearchResult: List<Song> = listOf(),
    val artistsSearchResult: List<Artist> = listOf(),
    val albumsSearchResult: List<Album> = listOf(),
    val icon: ImageBitmap? = null,
    val isPlaying: Boolean = false,
    val showPlayList: Boolean = false,
    val progress: Float = 0f,
    val isSearching: Boolean = false,
    val songDuration: Float = 1f,
    @RepeatMode val mode: Int = Player.REPEAT_MODE_OFF,
    val shuffle: Boolean = false
) {
    companion object {
        val INITIAL_STATE = AmuzicState()
    }
}

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val repo: SongsRepo,
    private val amuzicPlayer: AmuzicPlayer
) : ViewModel() {

    var state by mutableStateOf(INITIAL_STATE)
        private set

    private val _isLoadingSongs = mutableStateOf(false)
    private val _isLoaded = mutableStateOf(false)
    val isLoaded: State<Boolean> = _isLoaded

    var confirmExit = false
        private set

    private val _showPlayBar = mutableStateOf(false)
    val showPlayBar: State<Boolean> = _showPlayBar
    private var scrollValue = 0

    private var playBarDelayJob: Job? = null

    fun init(context: Context) {
        amuzicPlayer.onTransition = { index, duration ->
            state = state.copy(currentSong = state.songs[index], songDuration = duration, progress = 0f)
        }
        viewModelScope.launch { loadSongs(context) }
        startProgressMonitor()
    }

    fun confirmExit() {
        viewModelScope.launch {
            confirmExit = true
            delay(2000)
            confirmExit = false
        }
    }

    fun onSongClicked(index: Int) {
        state.apply {
            val song = if (isSearching) {
                songsSearchResult[index]
            } else {
                songs[index]
            }
            val actualIndex = if (isSearching) {
                songs.indexOf(song)
            } else {
                index
            }
            if (currentSong != song) {
                state = copy(currentSong = song, currentPlaylist = songs)
                amuzicPlayer.createPlayList(songs.map { it.item })
                amuzicPlayer.selectSong(actualIndex)
                onPlaySong()
            }
            if (!showPlayList) {
                showAndDelayHidePlayBar()
            }
        }
    }

    fun onPlayClicked() {
        if (amuzicPlayer.isActive()) {
            onPauseSong()
            return
        }
        onPlaySong()
    }

    fun onNextClicked() {
        amuzicPlayer.nextSong()
    }

    fun onPrevClicked() {
        amuzicPlayer.prevSong()
    }

    fun onArtistClicked(artist: Artist) {
        viewModelScope.launch {
            val songs = repo.songs.filter { song -> song.artist == artist.name }
            val icon = songs.firstNotNullOfOrNull { it.thumbnail }?.asImageBitmap()
            state = state.copy(currentArtist = artist, songs = songs, icon = icon)
        }
    }

    fun onAlbumClicked(album: Album) {
        viewModelScope.launch {
            val songs = repo.songs.filter { song -> song.album == album.name }
            val icon = songs.firstNotNullOfOrNull { it.thumbnail }?.asImageBitmap()
            state = state.copy(currentAlbum = album, songs = songs, icon = icon)
        }
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
        state = state.copy(mode = amuzicPlayer.switchMode(), shuffle = amuzicPlayer.shuffleMode)
    }

    fun onSeekTouch(position: Float) {
        state = state.copy(progress = position)
        amuzicPlayer.seekTo(position)
    }

    fun onToggleSearching() {
        viewModelScope.launch {
            state = state.copy(isSearching = !state.isSearching)
        }
    }

    fun onSearchSongs(query: String) {
        viewModelScope.launch {
            state = with(state) {
                copy(
                    songsSearchResult = songs.filter { song -> song.title.contains(query, true) }
                )
            }
        }
    }

    fun onSearchArtists(query: String) {
        viewModelScope.launch {
            state = with(state) {
                copy(
                    artistsSearchResult = artists.filter {
                            artist ->
                        artist.name.contains(query, true)
                    }
                )
            }
        }
    }

    fun onSearchAlbums(query: String) {
        viewModelScope.launch {
            state = with(state) {
                copy(
                    albumsSearchResult = albums.filter { album -> album.name.contains(query, true) }
                )
            }
        }
    }

    fun onNavigateToAllSongs() {
        viewModelScope.launch {
            state = state.copy(songs = repo.songs)
        }
    }

    fun onTogglePlayList(show: Boolean) {
        state = state.copy(showPlayList = show)
    }

    fun onExit() {
        amuzicPlayer.releasePlayer()
    }

    private fun showAndDelayHidePlayBar() {
        switchOnDelayOffBoolState(_showPlayBar)
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
            if (playBarDelayJob != null) {
                playBarDelayJob?.cancel()
                playBarDelayJob = null
            }
            playBarDelayJob = viewModelScope.launch {
                boolState.value = true
                delay(10000)
                boolState.value = false
            }
        }
    }

    private fun startProgressMonitor() {
        val progressHandler = Handler(Looper.getMainLooper())
        val progressUpdateRunnable = object : Runnable {
            override fun run() {
                progressHandler.removeCallbacks(this)
                if (amuzicPlayer.isActive()) {
                    val progress = amuzicPlayer.progress().coerceAtLeast(0f)
                    val duration = amuzicPlayer.duration().coerceAtLeast(0f)
                    state = state.copy(progress = progress / duration)
                }
                state = state.copy(isPlaying = amuzicPlayer.isActive())
                progressHandler.postDelayed(this, 200)
            }
        }
        progressHandler.postDelayed(progressUpdateRunnable, 200)
    }

    private fun onPlaySong() {
        amuzicPlayer.playSong()
    }

    private fun onPauseSong() {
        amuzicPlayer.pauseSong()
    }

    private fun loadSongs(context: Context) {
        viewModelScope.launch {
            repo.loadSongs(
                isLoading = {
                    _isLoadingSongs.value = true
                    amuzicPlayer.init(context)
                },
                onComplete = { songs ->
                    _isLoadingSongs.value = false
                    _isLoaded.value = true
                    state = state.copy(
                        currentSong = songs.first(),
                        songs = songs,
                        currentPlaylist = songs,
                        artists = repo.artists,
                        albums = repo.albums,
                        songsSearchResult = songs,
                        artistsSearchResult = repo.artists,
                        albumsSearchResult = repo.albums
                    )
                    launch(Dispatchers.Main) {
                        amuzicPlayer.createPlayList(songs.map { it.item })
                    }
                }
            )
        }
    }
}

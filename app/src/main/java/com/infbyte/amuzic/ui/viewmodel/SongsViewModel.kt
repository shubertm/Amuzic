package com.infbyte.amuzic.ui.viewmodel

import android.content.Context
import android.util.Log
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
import com.infbyte.amuzic.data.model.Playlist
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.data.repo.PlaylistsRepo
import com.infbyte.amuzic.data.repo.SongsRepo
import com.infbyte.amuzic.playback.AmuzicPlayer
import com.infbyte.amuzic.ui.viewmodel.AmuzicState.Companion.INITIAL_STATE
import com.infbyte.amuzic.utils.tryGetFirst
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.path.Path

data class AmuzicState(
    val currentSong: Song = Song(),
    val currentArtist: Artist = Artist(),
    val currentAlbum: Album = Album(),
    val songs: List<Song> = emptyList(),
    val currentPlaylist: List<Song> = emptyList(),
    val playlist: List<Song> = emptyList(),
    val selectedSongs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val newPlaylist: Playlist = Playlist(),
    val isCreatingPlaylist: Boolean = false,
    val songsSearchResult: List<Song> = emptyList(),
    val artistsSearchResult: List<Artist> = emptyList(),
    val albumsSearchResult: List<Album> = emptyList(),
    val icon: ImageBitmap? = null,
    val artistOrAlbumInitialChar: String = "",
    val isPlaying: Boolean = false,
    val showPlayList: Boolean = false,
    val progress: Float = 0f,
    val isSearching: Boolean = false,
    val songDuration: Float = 1f,
    @RepeatMode val mode: Int = Player.REPEAT_MODE_OFF,
    val shuffle: Boolean = false,
    val isReadPermGranted: Boolean = false,
    val isTermsAccepted: Boolean = true,
    val hasMusic: Boolean = false,
    val isLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSelecting: Boolean = false,
) {
    companion object {
        val INITIAL_STATE = AmuzicState()
    }
}

data class SideEffect(
    val showSplash: Boolean = true,
    val showPrivacyDialog: Boolean = false,
    val showAppSettingsRedirect: Boolean = false,
)

@HiltViewModel
class SongsViewModel
    @Inject
    constructor(
        private val repo: SongsRepo,
        private val amuzicPlayer: AmuzicPlayer,
        private val playlistsRepo: PlaylistsRepo,
    ) : ViewModel() {
        var state by mutableStateOf(INITIAL_STATE)
            private set

        var sideEffect by mutableStateOf(SideEffect())
            private set

        var confirmExit = false
            private set

        private val _showPlayBar = mutableStateOf(false)
        val showPlayBar: State<Boolean> = _showPlayBar
        private var scrollValue = 0

        private var playBarDelayJob: Job? = null

        init {
            amuzicPlayer.onTransition = { index, duration ->
                state =
                    with(state) {
                        copy(currentSong = currentPlaylist[index], songDuration = duration)
                    }
            }
            amuzicPlayer.sendIsPlaying = { isPlaying ->
                state = state.copy(isPlaying = isPlaying)
                state.currentSong.updateIsPlaying(isPlaying)
            }
            amuzicPlayer.sendProgress = { progress ->
                state = state.copy(progress = progress)
            }
        }

        fun init(context: Context) {
            viewModelScope.launch { playlistsRepo.init(Path(context.filesDir.path)) }
            refreshController(context)
            loadSongs()
            loadPlaylists()
        }

        fun confirmExit() {
            viewModelScope.launch {
                confirmExit = true
                delay(2000)
                confirmExit = false
            }
        }

        fun onSongClicked(song: Song) {
            if (state.isSelecting) {
                if (state.selectedSongs.contains(song)) {
                    removeFromSelected(song)
                    if (state.selectedSongs.isEmpty()) {
                        disableSelecting()
                    }
                    return
                }
                addToSelected(song)
                return
            }
            state.apply {
                val actualIndex = songs.indexOf(song)

                val position = if (currentSong == song) amuzicPlayer.progress().toLong() else 0L

                state = copy(currentSong = song, currentPlaylist = songs)
                amuzicPlayer.createPlayList(songs.map { it.item })
                amuzicPlayer.selectSong(actualIndex, position)

                onPlaySong()

                if (!showPlayList) {
                    showAndDelayHidePlayBar()
                }
            }
        }

        fun onSongLongClicked(song: Song) {
            if (!state.isSelecting) enableSelecting()
            if (state.isSelecting) {
                if (state.selectedSongs.contains(song)) {
                    removeFromSelected(song)
                    if (state.selectedSongs.isEmpty()) {
                        disableSelecting()
                    }
                    return
                }
                addToSelected(song)
                return
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
                state =
                    state.copy(
                        currentArtist = artist,
                        songs = songs,
                        icon = icon,
                        artistOrAlbumInitialChar = artist.name.first().toString(),
                    )
            }
        }

        fun onAlbumClicked(album: Album) {
            viewModelScope.launch {
                val songs = repo.songs.filter { song -> song.album == album.name }
                val icon = songs.firstNotNullOfOrNull { it.thumbnail }?.asImageBitmap()
                state =
                    state.copy(
                        currentAlbum = album,
                        songs = songs,
                        icon = icon,
                        artistOrAlbumInitialChar = album.name.first().toString(),
                    )
            }
        }

        fun onPlaylistClicked(list: Playlist) {
            viewModelScope.launch {
                val songs =
                    repo.songs.filter { song ->
                        list.songs.contains(song.id)
                    }
                state = state.copy(songs = songs)
            }
        }

        fun togglePlayBarByScroll(value: Int) {
            if (value != 0) {
                calcScrollDelta(value) { delta ->
                    togglePlayBarByScrollDelta(delta)
                }
            } else {
                showAndDelayHidePlayBar()
            }
        }

        fun hidePlayBar() {
            _showPlayBar.value = false
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

        fun onCreatePlaylist() {
            if (state.selectedSongs.isNotEmpty() && state.isCreatingPlaylist) {
                viewModelScope.launch {
                    val playlist =
                        state.newPlaylist.copy(
                            songs = state.selectedSongs.map { it.id },
                        )
                    playlistsRepo.add(playlist)
                    state =
                        state.copy(
                            selectedSongs = emptyList(),
                            newPlaylist = Playlist(),
                            playlists = playlistsRepo.getAll(),
                            isCreatingPlaylist = false,
                        )
                }
            }
        }

        fun updateNewPlaylist(name: String) {
            viewModelScope.launch {
                state = state.copy(newPlaylist = Playlist(name = name), isCreatingPlaylist = true)
            }
        }

        fun enableSelecting() {
            viewModelScope.launch {
                state = state.copy(isSelecting = true)
            }
        }

        fun disableSelecting() {
            viewModelScope.launch {
                state = state.copy(isSelecting = false, selectedSongs = emptyList())
            }
        }

        fun onSearchSongs(query: String) {
            viewModelScope.launch {
                state =
                    with(state) {
                        copy(
                            songsSearchResult = songs.filter { song -> song.title.contains(query, true) },
                        )
                    }
            }
        }

        fun onSearchArtists(query: String) {
            viewModelScope.launch {
                state =
                    with(state) {
                        copy(
                            artistsSearchResult =
                                artists.filter { artist ->
                                    artist.name.contains(query, true)
                                },
                        )
                    }
            }
        }

        fun onSearchAlbums(query: String) {
            viewModelScope.launch {
                state =
                    with(state) {
                        copy(
                            albumsSearchResult = albums.filter { album -> album.name.contains(query, true) },
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

        fun setReadPermGranted(granted: Boolean) {
            state = state.copy(isReadPermGranted = granted)
        }

        fun setTermsAccepted(accepted: Boolean) {
            state = state.copy(isTermsAccepted = accepted)
        }

        fun setIsRefreshing(isRefreshing: Boolean) {
            state = state.copy(isRefreshing = isRefreshing)
        }

        fun addToSelected(song: Song) {
            viewModelScope.launch {
                val songs = state.selectedSongs.toMutableList()
                songs.add(song)
                state = state.copy(selectedSongs = songs)
                Log.d("ViewModel", "selected ${state.selectedSongs.size} songs")
            }
        }

        fun removeFromSelected(song: Song) {
            viewModelScope.launch {
                val songs = state.selectedSongs.toMutableList()
                songs.remove(song)
                state = state.copy(selectedSongs = songs)
                Log.d("ViewModel", "selected ${state.selectedSongs.size} songs")
            }
        }

        fun onExit() {
            amuzicPlayer.releasePlayer()
        }

        fun showPrivacyDialog() {
            sideEffect = sideEffect.copy(showPrivacyDialog = true)
        }

        fun hidePrivacyDialog() {
            sideEffect = sideEffect.copy(showPrivacyDialog = false)
        }

        fun showAppSettingsRedirect() {
            sideEffect = sideEffect.copy(showAppSettingsRedirect = true)
        }

        fun hideAppSettingsRedirect() {
            sideEffect = sideEffect.copy(showAppSettingsRedirect = false)
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
            toggle: (Int) -> Unit,
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
                playBarDelayJob =
                    viewModelScope.launch {
                        boolState.value = true
                        delay(10000)
                        boolState.value = false
                    }
            }
        }

        private fun onPlaySong() {
            if (!amuzicPlayer.areSongsAvailable()) {
                amuzicPlayer.createPlayList(state.currentPlaylist.map { it.item })
                val index = state.currentPlaylist.indexOf(state.currentSong)
                val position = (state.progress * state.currentSong.duration).toLong()
                amuzicPlayer.selectSong(index, position)
            }
            amuzicPlayer.playSong()
        }

        private fun onPauseSong() {
            amuzicPlayer.pauseSong()
        }

        private fun loadSongs() {
            viewModelScope.launch {
                repo.loadSongs(
                    onComplete = { songs ->
                        viewModelScope.launch {
                            if (state.isRefreshing && songs.isEmpty()) {
                                delay(1000)
                            }
                            state =
                                state.copy(
                                    currentSong = songs.tryGetFirst { state.currentSong },
                                    songs = songs,
                                    currentPlaylist = songs,
                                    artists = repo.artists,
                                    albums = repo.albums,
                                    songsSearchResult = songs,
                                    artistsSearchResult = repo.artists,
                                    albumsSearchResult = repo.albums,
                                    isLoaded = true,
                                    hasMusic = songs.isNotEmpty(),
                                    isRefreshing = false,
                                )
                            sideEffect = sideEffect.copy(showSplash = false)
                            launch(Dispatchers.Main) {
                                amuzicPlayer.createPlayList(songs.map { it.item })
                            }
                        }
                    },
                )
            }
        }

        private fun loadPlaylists() {
            viewModelScope.launch {
                state = state.copy(playlists = playlistsRepo.getAll())
            }
        }

        private fun refreshController(context: Context) {
            amuzicPlayer.initController(context)
        }
    }

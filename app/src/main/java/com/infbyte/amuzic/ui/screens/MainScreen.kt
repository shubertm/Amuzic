package com.infbyte.amuzic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.ui.views.BannerAdView
import com.infbyte.amuzic.ui.views.PlaylistsBottomSheet
import com.infbyte.amuzic.utils.navigationBarsPadding
import com.infbyte.amuzic.utils.toDp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    songsViewModel: SongsViewModel,
    onNavigate: (String) -> Unit,
    onExit: () -> Unit,
    about: @Composable (() -> Unit) -> Unit,
) {
    var showAbout by rememberSaveable {
        mutableStateOf(false)
    }
    val pagerState = rememberPagerState(0) { 3 }
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchFocusRequester =
        remember {
            FocusRequester()
        }
    var heightPadding by rememberSaveable { mutableIntStateOf(0) }
    var showPlaylists by rememberSaveable { mutableStateOf(false) }

    if (showAbout) {
        about { showAbout = false }
        return
    }

    LaunchedEffect(pagerState.currentPage == 0) {
        songsViewModel.onNavigateToAllSongs()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                Modifier.fillMaxWidth().onSizeChanged {
                    heightPadding = it.height
                },
            ) {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            if (songsViewModel.state.isSearching) {
                                songsViewModel.onSearchSongs(searchQuery)
                            }
                            pagerState.animateScrollToPage(0, animationSpec = tween(500, 300))
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.ic_library_music), "") },
                    label = { Text(stringResource(R.string.amuzic_songs)) },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            if (songsViewModel.state.isSearching) {
                                songsViewModel.onSearchArtists(searchQuery)
                            }
                            pagerState.animateScrollToPage(1, animationSpec = tween(500, 300))
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.ic_artist), "") },
                    label = { Text(stringResource(R.string.amuzic_artists)) },
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch {
                            if (songsViewModel.state.isSearching) {
                                songsViewModel.onSearchAlbums(searchQuery)
                            }
                            pagerState.animateScrollToPage(2, animationSpec = tween(500, 300))
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.ic_album), "") },
                    label = { Text(stringResource(R.string.amuzic_albums)) },
                )
            }
        },
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                when (pagerState.currentPage) {
                                    0 -> songsViewModel.onSearchSongs(it)
                                    1 -> songsViewModel.onSearchArtists(it)
                                    2 -> songsViewModel.onSearchAlbums(it)
                                }
                            },
                            onSearch = {},
                            expanded = songsViewModel.state.isSearching,
                            onExpandedChange = {
                                if (!songsViewModel.state.isSearching) {
                                    searchQuery = ""
                                    songsViewModel.onToggleSearching()
                                }
                                when (pagerState.currentPage) {
                                    0 -> songsViewModel.onSearchSongs(searchQuery)
                                    1 -> songsViewModel.onSearchArtists(searchQuery)
                                    2 -> songsViewModel.onSearchAlbums(searchQuery)
                                }
                            },
                            placeholder = { Text(stringResource(R.string.amuzic_search)) },
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            if (!songsViewModel.state.isSearching) {
                                                songsViewModel.onToggleSearching()
                                                searchFocusRequester.requestFocus()
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Outlined.Search, "")
                                    }
                                    if (!songsViewModel.state.isSearching) {
                                        IconButton(
                                            onClick = {
                                                songsViewModel.hidePlayBar()
                                                showAbout = true
                                            },
                                        ) {
                                            Icon(Icons.Outlined.Info, contentDescription = "")
                                        }
                                    }
                                }
                            },
                        )
                    },
                    expanded = songsViewModel.state.isSearching,
                    onExpandedChange = {
                        if (!songsViewModel.state.isSearching) {
                            searchQuery = ""
                            songsViewModel.onToggleSearching()
                        }
                        when (pagerState.currentPage) {
                            0 -> songsViewModel.onSearchSongs(searchQuery)
                            1 -> songsViewModel.onSearchArtists(searchQuery)
                            2 -> songsViewModel.onSearchAlbums(searchQuery)
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                                end = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                            )
                            .navigationBarsPadding(songsViewModel.state.isSearching)
                            .focusRequester(searchFocusRequester),
                ) {
                    when (pagerState.currentPage) {
                        0 ->
                            if (songsViewModel.state.songsSearchResult.isEmpty()) {
                                com.infbyte.amuze.ui.screens.NoSearchResultScreen()
                            } else {
                                SongsScreen(
                                    songs = songsViewModel.state.songsSearchResult,
                                    songsViewModel.state.currentSong,
                                    songsViewModel.state.isSelecting,
                                    songsViewModel.state.isCreatingPlaylist,
                                    onScroll = { scrollValue ->
                                        songsViewModel.togglePlayBarByScroll(scrollValue)
                                    },
                                    onSongClick = { song ->
                                        searchQuery = ""
                                        songsViewModel.onSongClicked(song)
                                        songsViewModel.onToggleSearching()
                                    },
                                    onSongLongClick = { song ->
                                        songsViewModel.onSongLongClicked(song)
                                    },
                                    onSelectionDone = {
                                        songsViewModel.disableSelecting()
                                    },
                                )
                            }

                        1 ->
                            if (songsViewModel.state.artistsSearchResult.isEmpty()) {
                                com.infbyte.amuze.ui.screens.NoSearchResultScreen()
                            } else {
                                ArtistsScreen(
                                    artists = songsViewModel.state.artistsSearchResult,
                                    onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                                    onArtistClick = { artist ->
                                        searchQuery = ""
                                        songsViewModel.onToggleSearching()
                                        songsViewModel.onArtistClicked(artist)
                                        onNavigate(Screens.SONGS)
                                    },
                                )
                            }

                        2 ->
                            if (songsViewModel.state.albumsSearchResult.isEmpty()) {
                                com.infbyte.amuze.ui.screens.NoSearchResultScreen()
                            } else {
                                AlbumsScreen(
                                    albums = songsViewModel.state.albumsSearchResult,
                                    onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                                    onAlbumClicked = { album ->
                                        searchQuery = ""
                                        songsViewModel.onToggleSearching()
                                        songsViewModel.onAlbumClicked(album)
                                        onNavigate(Screens.SONGS)
                                    },
                                )
                            }
                    }
                }
                BannerAdView()
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
        ) { page ->
            when (page) {
                0 -> {
                    SongsScreen(
                        songs = songsViewModel.state.songs,
                        songsViewModel.state.currentSong,
                        songsViewModel.state.isSelecting,
                        songsViewModel.state.isCreatingPlaylist,
                        onScroll = { scrollValue ->
                            songsViewModel.togglePlayBarByScroll(scrollValue)
                        },
                        onSongClick = { song ->
                            songsViewModel.onSongClicked(song)
                        },
                        onSongLongClick = { song ->
                            songsViewModel.onSongLongClicked(song)
                        },
                        onSelectionDone = {
                            songsViewModel.onCreatePlaylist()
                            songsViewModel.disableSelecting()
                        },
                    )
                }

                1 ->
                    ArtistsScreen(
                        artists = songsViewModel.state.artists,
                        onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                        onArtistClick = { artist ->
                            songsViewModel.onArtistClicked(artist)
                            onNavigate(Screens.SONGS)
                        },
                    )

                2 ->
                    AlbumsScreen(
                        albums = songsViewModel.state.albums,
                        onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                        onAlbumClicked = { album ->
                            songsViewModel.onAlbumClicked(album)
                            onNavigate(Screens.SONGS)
                        },
                    )
            }
        }
        Box(Modifier.fillMaxSize()) {
            if (!showPlaylists && !songsViewModel.state.isSelecting) {
                FloatingActionButton(
                    onClick = { showPlaylists = true },
                    Modifier.align(Alignment.BottomEnd).padding(
                        bottom = heightPadding.toDp() + 16.dp,
                        end = 16.dp,
                    ),
                ) {
                    Icon(painterResource(R.drawable.ic_queue_music), "")
                }
            }

            AnimatedVisibility(
                showPlaylists,
                Modifier.align(Alignment.BottomCenter),
                enter = expandVertically(expandFrom = Alignment.Bottom),
            ) {
                PlaylistsBottomSheet(
                    songsViewModel.state.playlists,
                    onAddPlaylist = { name ->
                        if (name.isNotEmpty()) {
                            songsViewModel.enableSelecting()
                            songsViewModel.updateNewPlaylist(name)
                            showPlaylists = false
                        }
                    },
                    onClickPlaylist = { list ->
                        songsViewModel.onPlaylistClicked(list)
                    },
                    onDeletePlaylist = { list ->
                        songsViewModel.updateNewPlaylist(list.name)
                        songsViewModel.onDeletePlaylist()
                    },
                ) { showPlaylists = false }
            }
        }
        BackHandler {
            if (songsViewModel.state.isSelecting) {
                songsViewModel.disableSelecting()
                return@BackHandler
            }

            if (songsViewModel.state.showPlayList) {
                songsViewModel.onTogglePlayList(false)
                return@BackHandler
            }

            if (songsViewModel.state.isSearching) {
                songsViewModel.onToggleSearching()
                searchQuery = ""
                return@BackHandler
            }
            onExit()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NavBar() {
    AmuzicTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = "",
                            onQueryChange = {},
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            Modifier.fillMaxWidth(),
                            leadingIcon = {
                                IconButton(
                                    onClick = { /*TODO*/ },
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "")
                                }
                            },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { /*TODO*/ }) {
                                        Icon(Icons.Outlined.Search, "")
                                    }
                                    IconButton(onClick = { /*TODO*/ }) {
                                        Box(
                                            Modifier
                                                .size(32.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    CircleShape,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                "Artist".first().toString(),
                                                style = MaterialTheme.typography.titleLarge,
                                            )
                                        }
                                    }
                                }
                            },
                            placeholder = { Text(stringResource(R.string.amuzic_search)) },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                ) {}
            }
            NavigationBar(Modifier.fillMaxWidth()) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_library_music), "") },
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_artist), "") },
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_album), "") },
                )
            }
        }
    }
}

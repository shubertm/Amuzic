package com.infbyte.amuzic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    songsViewModel: SongsViewModel,
    onNavigate: (String) -> Unit,
    onExit: () -> Unit
) {
    Box(Modifier.wrapContentHeight()) {
        val pagerState = rememberPagerState(0) { 3 }
        val scope = rememberCoroutineScope()
        var searchQuery by rememberSaveable { mutableStateOf("") }

        Column {
            SearchBar(
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
                active = songsViewModel.state.isSearching,
                onActiveChange = {
                    songsViewModel.onToggleSearching()
                    if (!songsViewModel.state.isSearching) { searchQuery = "" }
                    when (pagerState.currentPage) {
                        0 -> songsViewModel.onSearchSongs(searchQuery)
                        1 -> songsViewModel.onSearchArtists(searchQuery)
                        2 -> songsViewModel.onSearchAlbums(searchQuery)
                    }
                },
                Modifier.padding(start = 8.dp, end = 8.dp),
                trailingIcon = { Icon(Icons.Outlined.Search, "") },
                placeholder = { Text(stringResource(R.string.amuzic_search)) }
            ) {
                when (pagerState.currentPage) {
                    0 -> if (songsViewModel.state.songsSearchResult.isEmpty()) {
                        NoSearchResultScreen()
                    } else {
                        SongsScreen(
                            songs = songsViewModel.state.songsSearchResult,
                            onScroll = { scrollValue ->
                                songsViewModel.togglePlayBarByScroll(scrollValue)
                            },
                            onSongClick = { songIndex ->
                                searchQuery = ""
                                songsViewModel.onSongClicked(songIndex)
                                songsViewModel.onToggleSearching()
                            }
                        )
                    }

                    1 -> if (songsViewModel.state.artistsSearchResult.isEmpty()) {
                        NoSearchResultScreen()
                    } else {
                        ArtistsScreen(
                            artists = songsViewModel.state.artistsSearchResult,
                            onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                            onArtistClick = { artist ->
                                searchQuery = ""
                                songsViewModel.onToggleSearching()
                                songsViewModel.onArtistClicked(artist)
                                onNavigate(Screens.SONGS)
                            }
                        )
                    }

                    2 -> if (songsViewModel.state.albumsSearchResult.isEmpty()) {
                        NoSearchResultScreen()
                    } else {
                        AlbumsScreen(
                            albums = songsViewModel.state.albumsSearchResult,
                            onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                            onAlbumClicked = { album ->
                                searchQuery = ""
                                songsViewModel.onToggleSearching()
                                songsViewModel.onAlbumClicked(album)
                                onNavigate(Screens.SONGS)
                            }
                        )
                    }
                }
            }
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> {
                        LaunchedEffect(key1 = "") {
                            songsViewModel.onNavigateToAllSongs()
                        }
                        SongsScreen(
                            songs = songsViewModel.state.songs,
                            onScroll = { scrollValue ->
                                songsViewModel.togglePlayBarByScroll(scrollValue)
                            },
                            onSongClick = { songIndex ->
                                songsViewModel.onSongClicked(songIndex)
                            }
                        )
                    }

                    1 -> ArtistsScreen(
                        artists = songsViewModel.state.artists,
                        onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                        onArtistClick = { artist ->
                            songsViewModel.onArtistClicked(artist)
                            onNavigate(Screens.SONGS)
                        }
                    )

                    2 -> AlbumsScreen(
                        albums = songsViewModel.state.albums,
                        onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                        onAlbumClicked = { album ->
                            songsViewModel.onAlbumClicked(album)
                            onNavigate(Screens.SONGS)
                        }
                    )
                }
            }
        }
        NavigationBar(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
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
                label = { Text(stringResource(R.string.amuzic_songs)) }
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
                label = { Text(stringResource(R.string.amuzic_artists)) }
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
                label = { Text(stringResource(R.string.amuzic_albums)) }
            )
        }
        BackHandler {
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
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearch = {},
                active = false,
                onActiveChange = {},
                Modifier.padding(start = 8.dp, end = 8.dp),
                trailingIcon = { Icon(Icons.Outlined.Search, "") },
                placeholder = { Text(stringResource(R.string.amuzic_search)) }
            ) {}
            NavigationBar(Modifier.fillMaxWidth()) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_library_music), "") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_artist), "") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = { Icon(painterResource(R.drawable.ic_album), "") }
                )
            }
        }
    }
}

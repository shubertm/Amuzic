package com.infbyte.amuzic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.ui.views.BannerAdView
import com.infbyte.amuzic.ui.views.SelectionCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistOrAlbumSongsScreen(
    songsViewModel: SongsViewModel,
    onNavigateBack: () -> Unit,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val searchFocusRequester =
        remember {
            FocusRequester()
        }

    Column(Modifier.navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        songsViewModel.onSearchSongs(it)
                    },
                    onSearch = {},
                    expanded = songsViewModel.state.isSearching,
                    onExpandedChange = {
                        if (!songsViewModel.state.isSearching) {
                            searchQuery = ""
                            songsViewModel.onToggleSearching()
                        }
                        songsViewModel.onSearchSongs(searchQuery)
                    },
                    Modifier.fillMaxWidth().padding(
                        start = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                        end = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                    ).focusRequester(searchFocusRequester),
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                if (songsViewModel.state.isSearching) {
                                    searchQuery = ""
                                    songsViewModel.onToggleSearching()
                                    return@IconButton
                                }
                                onNavigateBack()
                            },
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "")
                        }
                    },
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
                                IconButton(onClick = {}) {
                                    songsViewModel.state.icon?.let { thumbnail ->
                                        Image(
                                            thumbnail,
                                            contentDescription = "",
                                            Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                        )
                                    } ?: Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            songsViewModel.state.artistOrAlbumInitialChar,
                                            style = MaterialTheme.typography.titleLarge,
                                        )
                                    }
                                }
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.amuzic_search)) },
                )
            },
            expanded = songsViewModel.state.isSearching,
            onExpandedChange = {
                if (!songsViewModel.state.isSearching) {
                    searchQuery = ""
                    songsViewModel.onToggleSearching()
                }
                songsViewModel.onSearchSongs(searchQuery)
            },
            modifier =
                Modifier.fillMaxWidth().padding(
                    start = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                    end = if (!songsViewModel.state.isSearching) 8.dp else 0.dp,
                ).focusRequester(searchFocusRequester),
        ) {
            if (songsViewModel.state.songsSearchResult.isNotEmpty()) {
                SongsScreen(
                    songsViewModel,
                    songs = songsViewModel.state.songsSearchResult,
                    songsViewModel.state.currentSong,
                    songsViewModel.state.isSelecting,
                    songsViewModel.state.isCreatingPlaylist,
                    onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                    onSongClick = { song ->
                        searchQuery = ""
                        songsViewModel.onSongClicked(song)
                        songsViewModel.onToggleSearching()
                    },
                    onSongLongClick = { song ->
                        songsViewModel.onSongLongClicked(song)
                    },
                    onSelectionDone = {
                        if (songsViewModel.state.isCreatingPlaylist) {
                            songsViewModel.showPlaylists()
                            songsViewModel.onCreatePlaylist()
                            songsViewModel.disableSelecting()
                        }
                    },
                    onSaveQuickPlaylist = { name ->
                        if (name.isNotEmpty()) {
                            songsViewModel.showPlaylists()
                            songsViewModel.updateNewPlaylist(name)
                            songsViewModel.onCreatePlaylist()
                            songsViewModel.disableSelecting()
                        }
                    },
                    onDismissQuickPlaylist = {
                        songsViewModel.disableSelecting()
                    },
                )
            } else {
                com.infbyte.amuze.ui.screens.NoSearchResultScreen()
            }
        }
        if (songsViewModel.state.isSelecting) {
            SelectionCount(songsViewModel.state.selectedSongs.size) {
                songsViewModel.disableSelecting()
                if (songsViewModel.state.isCreatingPlaylist) {
                    songsViewModel.stopCreatingPlaylist()
                }
            }
        }
        if (!songsViewModel.state.isSelecting) {
            BannerAdView()
        }
        SongsScreen(
            songsViewModel,
            songs = songsViewModel.state.songs,
            songsViewModel.state.currentSong,
            songsViewModel.state.isSelecting,
            songsViewModel.state.isCreatingPlaylist,
            onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
            onSongClick = { song ->
                songsViewModel.onSongClicked(song)
            },
            onSongLongClick = { song ->
                songsViewModel.onSongLongClicked(song)
            },
            onSelectionDone = {
                if (songsViewModel.state.isCreatingPlaylist) {
                    songsViewModel.showPlaylists()
                    songsViewModel.onCreatePlaylist()
                    songsViewModel.disableSelecting()
                }
            },
            onSaveQuickPlaylist = { name ->
                if (name.isNotEmpty()) {
                    songsViewModel.showPlaylists()
                    songsViewModel.updateNewPlaylist(name)
                    songsViewModel.onCreatePlaylist()
                    songsViewModel.disableSelecting()
                }
            },
            onDismissQuickPlaylist = {
                songsViewModel.disableSelecting()
            },
        )
    }

    BackHandler {
        if (songsViewModel.state.isSelecting) {
            songsViewModel.disableSelecting()
            if (songsViewModel.state.isCreatingPlaylist) {
                songsViewModel.stopCreatingPlaylist()
            }
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
        onNavigateBack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewSearchBar() {
    AmuzicTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = "",
                            onQueryChange = {},
                            onSearch = {},
                            expanded = false,
                            onExpandedChange = {},
                            Modifier.fillMaxWidth(0.894f),
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
        }
    }
}

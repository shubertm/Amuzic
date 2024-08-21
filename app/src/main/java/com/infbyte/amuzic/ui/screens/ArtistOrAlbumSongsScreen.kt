package com.infbyte.amuzic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistOrAlbumSongsScreen(
    songsViewModel: SongsViewModel,
    onNavigateBack: () -> Unit
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val searchFocusRequester = remember {
        FocusRequester()
    }

    Column {
        Row(Modifier.padding(start = 8.dp, end = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    songsViewModel.onSearchSongs(it)
                },
                onSearch = {},
                active = songsViewModel.state.isSearching,
                onActiveChange = {
                    if (!songsViewModel.state.isSearching) {
                        searchQuery = ""
                        songsViewModel.onToggleSearching()
                    }
                    songsViewModel.onSearchSongs(searchQuery)
                },
                Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                leadingIcon = {
                    IconButton(
                        onClick = {
                            if (songsViewModel.state.isSearching) {
                                searchQuery = ""
                                songsViewModel.onToggleSearching()
                                return@IconButton
                            }
                            onNavigateBack()
                        }
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
                            }
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
                                            .clip(CircleShape)
                                    )
                                } ?: Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        songsViewModel.state.artistOrAlbumInitialChar,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.amuzic_search)) }
            ) {
                if (songsViewModel.state.songsSearchResult.isNotEmpty()) {
                    SongsScreen(
                        songs = songsViewModel.state.songsSearchResult,
                        onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
                        onSongClick = { songIndex ->
                            searchQuery = ""
                            songsViewModel.onSongClicked(songIndex)
                            songsViewModel.onToggleSearching()
                        }
                    )
                } else {
                    NoSearchResultScreen()
                }
            }
        }
        SongsScreen(
            songs = songsViewModel.state.songs,
            onScroll = { scrollValue -> songsViewModel.togglePlayBarByScroll(scrollValue) },
            onSongClick = { songIndex ->
                songsViewModel.onSongClicked(songIndex)
            }
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
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    Modifier.fillMaxWidth(0.894f),
                    leadingIcon = {
                        IconButton(
                            onClick = { /*TODO*/ }
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
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Artist".first().toString(), style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.amuzic_search)) }
                ) {}
            }
        }
    }
}

package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.SongsViewModel
import com.infbyte.amuzic.ui.views.NewQuickPlaylist
import com.infbyte.amuzic.utils.accommodateFullBannerAds
import com.infbyte.amuzic.utils.calcScroll
import com.infbyte.amuzic.utils.getInitialChar

@Composable
fun SongsScreen(songsViewModel: SongsViewModel) {
    val state = rememberLazyListState()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), state) {
            accommodateFullBannerAds(
                if (songsViewModel.state.isSearching) {
                    songsViewModel.state.songsSearchResult
                } else {
                    songsViewModel.state.songs
                },
                showOnTopWithFewItems = false,
            ) { song ->
                Song(
                    song,
                    song == songsViewModel.state.currentSong,
                    songsViewModel.state.isSelecting,
                    onClick = {
                        songsViewModel.onSongClicked(song)
                    },
                    onLongClick = {
                        songsViewModel.onSongLongClicked(song)
                    },
                )
            }
        }
        if (songsViewModel.state.isSelecting && !songsViewModel.state.isCreatingPlaylist) {
            NewQuickPlaylist(
                Modifier.align(Alignment.BottomCenter),
                onSave = { name ->
                    if (name.isNotEmpty()) {
                        songsViewModel.showPlaylists()
                        songsViewModel.updateNewPlaylist(name)
                        songsViewModel.onCreatePlaylist()
                        songsViewModel.disableSelecting()
                    }
                },
                onDismiss = {
                    songsViewModel.disableSelecting()
                },
            )
        }
        if (songsViewModel.state.isCreatingPlaylist) {
            Button(
                onClick = {
                    if (songsViewModel.state.isCreatingPlaylist) {
                        songsViewModel.showPlaylists()
                        songsViewModel.onCreatePlaylist()
                        songsViewModel.disableSelecting()
                    }
                },
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 16.dp,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Check, "")
                    Text(stringResource(R.string.amuzic_done), Modifier.padding(start = 2.dp))
                }
            }
        }
        if (!songsViewModel.sideEffect.showPlaylists && !songsViewModel.state.isSelecting) {
            FloatingActionButton(
                onClick = { songsViewModel.showPlaylists() },
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                Icon(painterResource(R.drawable.ic_queue_music), "")
            }
        }
    }
    if (state.isScrollInProgress) {
        songsViewModel.togglePlayBarByScroll(calcScroll(state))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Song(
    song: Song,
    isCurrent: Boolean,
    isSelecting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition()

    val color =
        infiniteTransition.animateColor(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primary,
            InfiniteRepeatableSpec(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    var isSelected by rememberSaveable {
        mutableStateOf(false)
    }

    SideEffect {
        if (!isSelecting) {
            isSelected = false
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10))
            .combinedClickable(
                onClick = {
                    if (isSelecting) {
                        isSelected = !isSelected
                    }
                    onClick()
                },
                onLongClick = {
                    isSelected = true
                    onLongClick()
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (song.thumbnail != null) {
            Box(Modifier.padding(8.dp)) {
                Image(
                    bitmap = song.thumbnail.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .size(48.dp),
                    contentDescription = "",
                )
            }
        } else {
            Box(
                Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    song.title.getInitialChar(),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
        Column(
            Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                song.title,
                maxLines = 1,
            )
            Text(
                song.artist,
                Modifier.padding(5.dp),
                fontSize = 12.sp,
                maxLines = 1,
            )
        }

        if (isCurrent) {
            Icon(
                if (song.isPlaying) {
                    Icons.Outlined.PlayArrow
                } else {
                    ImageVector.vectorResource(R.drawable.ic_pause)
                },
                "",
                Modifier.padding(8.dp),
                tint = color.value,
            )
        }
        if (isSelecting) {
            Checkbox(
                isSelected,
                onCheckedChange = { checked ->
                    onClick()
                    isSelected = checked
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewSong() {
    AmuzicTheme {
        Song(song = Song(), true, true, {}, {})
    }
}

package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.accommodateFullBannerAds

@Composable
fun BoxScope.PlayListScreen(
    show: Boolean,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    AnimatedVisibility(
        visible = show,
        Modifier.align(Alignment.BottomCenter),
        enter = expandHorizontally(
            tween(durationMillis = 1000, delayMillis = 200),
            expandFrom = Alignment.Start
        ),
        exit = shrinkHorizontally(tween(durationMillis = 1000), shrinkTowards = Alignment.End)
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize().navigationBarsPadding().statusBarsPadding()
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        ) {
            accommodateFullBannerAds(songs) { song ->
                Song(song) {
                    onSongClick(song)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPlayListScreen() {
    AmuzicTheme {
        Box {
            PlayListScreen(true, listOf(Song(), Song(), Song()), onSongClick = {})
        }
    }
}

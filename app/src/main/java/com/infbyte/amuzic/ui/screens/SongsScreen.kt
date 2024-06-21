package com.infbyte.amuzic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.utils.calcScroll

@Composable
fun SongsScreen(
    songs: List<Song>,
    onScroll: (Int) -> Unit,
    onSongClick: (Int) -> Unit
) {
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize(), state) {
        itemsIndexed(songs) { index, song ->
            Song(song) {
                onSongClick(index)
            }
        }
    }
    if (state.isScrollInProgress) {
        onScroll(calcScroll(state))
    }
}

@Composable
fun Song(song: Song, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (song.thumbnail != null) {
            Box(Modifier.padding(8.dp)) {
                Image(
                    bitmap = song.thumbnail.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp),
                    contentDescription = ""
                )
            }
        } else {
            Box(Modifier.padding(8.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_audiotrack),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp),
                    contentDescription = ""
                )
            }
        }
        Column(
            Modifier
                .wrapContentSize()
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                song.title,
                maxLines = 1
            )
            Text(
                song.artist,
                Modifier.padding(5.dp),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

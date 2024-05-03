package com.infbyte.amuzic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.utils.calcScroll

@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    onScroll: (Int) -> Unit,
    onArtistClick: (Int) -> Unit
) {
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize(), state) {
        itemsIndexed(artists) { index, artist ->
            Artist(artist) {
                onArtistClick(index)
            }
        }
    }
    if (state.isScrollInProgress) {
        onScroll(calcScroll(state))
    }
}

@Composable
fun Artist(
    artist: Artist,
    onClick: () -> Unit
) {
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
        Box(Modifier.padding(8.dp)) {
            Image(
                ImageVector.vectorResource(R.drawable.ic_artist),
                "",
                Modifier.size(48.dp)
            )
        }
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp)
        ) {
            Text(
                artist.name
            )
            Text(
                artist.numberOfSongs.toString(),
                Modifier.padding(start = 5.dp)
            )
        }
    }
}

package com.infbyte.amuzic.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.data.model.Artist
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.calcScroll
import com.infbyte.amuzic.utils.getInitialChar

@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    onScroll: (Int) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize(), state) {
        itemsIndexed(artists) { index, artist ->
            Artist(artist) {
                onArtistClick(artist)
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
        Box(
            Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(artist.name.getInitialChar(), style = MaterialTheme.typography.headlineLarge)
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

@Preview(showBackground = true)
@Composable
fun PreviewArtistsScreen() {
    AmuzicTheme {
        ArtistsScreen(
            listOf(
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist"),
                Artist("artist")
            ),
            {}
        ) { }
    }
}

@Preview
@Composable
fun PreviewArtist() {
    AmuzicTheme {
        Artist(artist = Artist("artist")) {}
    }
}

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
import com.infbyte.amuzic.data.model.Album
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.accommodateFullBannerAds
import com.infbyte.amuzic.utils.calcScroll
import com.infbyte.amuzic.utils.getInitialChar

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onScroll: (Int) -> Unit,
    onAlbumClicked: (Album) -> Unit,
) {
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize(), state) {
        accommodateFullBannerAds(albums, showOnTopWithFewItems = false) { album ->
            Album(album) {
                onAlbumClicked(album)
            }
        }
    }
    if (state.isScrollInProgress) {
        onScroll(calcScroll(state))
    }
}

@Composable
fun Album(
    album: Album,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10))
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).size(
                48.dp,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(album.name.getInitialChar(), style = MaterialTheme.typography.headlineLarge)
        }
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp),
        ) {
            Text(
                album.name,
            )
            Text(
                album.numberOfSongs.toString(),
                Modifier.padding(start = 5.dp),
            )
        }
    }
}

@Preview
@Composable
fun PreviewAlbum() {
    AmuzicTheme {
        Album(album = Album("My album")) {}
    }
}

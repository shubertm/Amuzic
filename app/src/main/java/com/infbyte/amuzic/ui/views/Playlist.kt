package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.data.model.Playlist
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.getInitialChar

@Composable
fun Playlist(
    item: Playlist = Playlist(),
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(10))
            .clip(RoundedCornerShape(10))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.name.getInitialChar(), style = MaterialTheme.typography.headlineLarge)
        }
        Text(
            item.name,
            Modifier.fillMaxWidth().weight(1f),
        )
        IconButton(
            onClick = { onDelete() },
        ) {
            Icon(Icons.Outlined.Delete, "")
        }
    }
}

@Preview
@Composable
fun PreviewPlaylist() {
    AmuzicTheme {
        com.infbyte.amuzic.ui.views.Playlist()
    }
}

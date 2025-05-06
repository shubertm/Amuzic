package com.infbyte.amuzic.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Playlist
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsBottomSheet(
    list: List<Playlist> = emptyList(),
    onClickPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddPlaylist: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showNewPlaylist by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismiss,
        Modifier.imePadding(),
    ) {
        androidx.compose.animation.AnimatedVisibility(showNewPlaylist) {
            NewPlaylist(
                onSave = { name -> onAddPlaylist(name) },
                onDismiss = { showNewPlaylist = false },
            )
        }

        LazyColumn(Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
            items(list) { playlist ->
                Playlist(
                    playlist,
                    onClick = {
                        onClickPlaylist(playlist)
                    },
                    onDelete = {
                        onDeletePlaylist(playlist)
                    },
                )
            }
        }

        if (!showNewPlaylist) {
            ElevatedButton(
                onClick = { showNewPlaylist = true },
                Modifier.align(Alignment.CenterHorizontally).padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Add, "")
                    Text(stringResource(R.string.amuzic_new))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPlaylists() {
    AmuzicTheme {
        PlaylistsBottomSheet(listOf(Playlist(), Playlist(), Playlist()), {}, {}, {}, {})
    }
}

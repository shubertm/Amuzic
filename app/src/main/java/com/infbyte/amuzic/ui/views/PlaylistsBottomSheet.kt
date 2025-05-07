package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Playlist
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.toDp

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
    var dragHandleHeight by rememberSaveable { mutableIntStateOf(0) }
    val dragHandleTopPadding = remember { 8.dp }
    val statusBarsPaddingValues = WindowInsets.statusBars.asPaddingValues()

    ModalBottomSheet(
        onDismiss,
        Modifier.imePadding(),
        dragHandle = {
            Surface(
                Modifier.padding(top = dragHandleTopPadding).onSizeChanged { newSize ->
                    dragHandleHeight = newSize.height
                },
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Box(
                    Modifier.size(32.dp, 5.dp),
                )
            }
        },
    ) {
        androidx.compose.animation.AnimatedVisibility(showNewPlaylist) {
            NewPlaylist(
                onSave = { name -> onAddPlaylist(name) },
                onDismiss = { showNewPlaylist = false },
            )
        }

        if (!showNewPlaylist) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(
                        top =
                            (
                                statusBarsPaddingValues.calculateTopPadding() - dragHandleHeight.toDp() -
                                    dragHandleTopPadding
                            )
                                .coerceAtLeast(0.dp),
                        start = 8.dp,
                        end = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.amuzic_playlists),
                    Modifier.fillMaxWidth().weight(1f).padding(start = 8.dp, end = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                FilledTonalIconButton(
                    onClick = { showNewPlaylist = true },
                ) {
                    Icon(Icons.Outlined.Add, "")
                }
            }
        }

        LazyColumn(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
    }
}

@Preview
@Composable
fun PreviewPlaylists() {
    AmuzicTheme {
        PlaylistsBottomSheet(listOf(Playlist(), Playlist(), Playlist()), {}, {}, {}, {})
    }
}

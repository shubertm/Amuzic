package com.infbyte.amuzic.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Playlist
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.utils.getInitialChar

@Composable
fun Playlist(
    item: Playlist = Playlist("Playlist 1"),
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    var isDeleting by rememberSaveable { mutableStateOf(false) }
    var isDeleteConfirmed by rememberSaveable { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        if (isDeleteConfirmed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(durationMillis = 2_500),
        finishedListener = {
            if (isDeleteConfirmed) {
                onDelete()
                isDeleteConfirmed = false
            }
        },
    )

    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .background(
                if (isDeleteConfirmed) backgroundColor else MaterialTheme.colorScheme.surfaceContainerLow,
                RoundedCornerShape(10),
            )
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
            Text(
                item.name.getInitialChar(),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Text(
            item.name,
            Modifier.fillMaxWidth().weight(1f),
        )
        if (isDeleting) {
            TextButton(
                onClick = { isDeleting = false },
                Modifier.padding(end = 8.dp),
            ) {
                Text(
                    stringResource(R.string.amuzic_no),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(
                onClick = {
                    isDeleteConfirmed = true
                    isDeleting = false
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(
                    stringResource(R.string.amuzic_delete),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            return@Row
        }
        if (isDeleteConfirmed) {
            Text(
                stringResource(R.string.amuzic_deleted),
                Modifier.padding(end = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            return@Row
        }
        IconButton(
            onClick = { isDeleting = true },
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

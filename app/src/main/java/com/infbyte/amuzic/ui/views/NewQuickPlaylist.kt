package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun NewQuickPlaylist(
    modifier: Modifier = Modifier,
    onSave: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val focusRequester = remember { FocusRequester() }
        var name by rememberSaveable { mutableStateOf("") }

        LaunchedEffect("") {
            focusRequester.requestFocus()
        }

        TextField(
            name,
            onValueChange = { name = it },
            Modifier.focusRequester(focusRequester)
                .fillMaxWidth().weight(1f).imePadding(),
            label = { Text(stringResource(R.string.amuzic_quick_playlist)) },
            leadingIcon = {
                IconButton(
                    onClick = {
                        name = ""
                        onDismiss()
                    },
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        "",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            },
            trailingIcon = {
                FilledTonalIconButton(
                    onClick = { onSave(name) },
                ) {
                    Icon(Icons.Outlined.Check, "")
                }
            },
        )
    }
}

@Preview
@Composable
fun PreviewNewQuickPlaylist() {
    AmuzicTheme {
        NewQuickPlaylist()
    }
}

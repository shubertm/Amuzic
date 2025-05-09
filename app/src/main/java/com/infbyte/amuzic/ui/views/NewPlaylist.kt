package com.infbyte.amuzic.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun NewPlaylist(
    onSave: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    var name by rememberSaveable { mutableStateOf("") }

    LaunchedEffect("") {
        focusRequester.requestFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            name,
            onValueChange = { name = it },
            Modifier.fillMaxWidth().weight(1f)
                .focusRequester(focusRequester)
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp).imePadding(),
            label = { Text(stringResource(R.string.amuzic_name)) },
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
                    Modifier.padding(end = 8.dp),
                ) {
                    Icon(Icons.Outlined.Add, "")
                }
            },
        )
    }
}

@Preview
@Composable
fun PreviewNewPlaylist() {
    AmuzicTheme {
        NewPlaylist()
    }
}

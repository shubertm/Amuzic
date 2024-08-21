package com.infbyte.amuzic.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme

@Composable
fun NoMediaPermissionScreen(
    onStartListening: () -> Unit,
    onExit: () -> Unit,
    about: @Composable (() -> Unit) -> Unit
) {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var showAbout by rememberSaveable { mutableStateOf(false) }

        if (showAbout) {
            about { showAbout = false }
            return
        }

        IconButton(
            onClick = { showAbout = true },
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Icon(Icons.Outlined.Info, contentDescription = "")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Text(
                    text = "Get ready for",
                    Modifier.padding(top = 31.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                ) // stringResource(id = R.string.amuzic_intro_1))
                Icon(
                    painter = painterResource(R.drawable.ic_amuzic_splash_foreground),
                    contentDescription = "",
                    Modifier
                        .padding(start = 112.dp)
                        .size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "muzement",
                    Modifier.padding(start = 162.dp, top = 31.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                ) // stringResource(R.string.amuzic_intro_2))
            }
            Button(
                onClick = { onStartListening() },
                Modifier.padding(top = 64.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                elevation = ButtonDefaults.filledTonalButtonElevation()
            ) {
                Text("Listen")
            }
        }
    }

    BackHandler {
        onExit()
    }
}

@Preview
@Composable
fun PreviewNoMediaPermissionScreen() {
    AmuzicTheme {
        NoMediaPermissionScreen({}, {}, {})
    }
}

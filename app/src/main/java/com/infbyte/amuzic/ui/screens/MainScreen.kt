package com.infbyte.amuzic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(
    showTopBar: State<Boolean>,
    showPopup: Boolean,
    screen: String,
    onTogglePopup: () -> Unit,
    onNavigateToScreen: (String) -> Unit,
    content: @Composable () -> Unit
) {
    content()
    Box(Modifier.fillMaxSize()) {
        TopBar(
            screen,
            showTopBar.value
        ) {
            onTogglePopup()
        }

        ScreenOptionsPopup(
            showPopup,
            onToggle = {
                onTogglePopup()
            }
        ) {
            onNavigateToScreen(it)
        }
    }
}

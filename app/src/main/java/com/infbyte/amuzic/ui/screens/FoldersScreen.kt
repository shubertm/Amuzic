package com.infbyte.amuzic.ui.screens

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Folder
import com.infbyte.amuzic.ui.theme.CreamWhite
import com.infbyte.amuzic.utils.calcScroll

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FoldersScreen(
    isVisible: Boolean,
    showPopup: MutableState<Boolean>,
    folders: List<Folder>,
    onScroll: (Int) -> Unit,
    onFolderClicked: (Int) -> Unit
) {
    AnimatedVisibility(
        isVisible,
        enter = fadeIn(tween(1000)),
        exit = fadeOut(tween(1000))
    ) {
        val state = rememberLazyListState()
        val modifier = if (showPopup.value) {
            Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    if (it.action == MotionEvent.ACTION_DOWN) {
                        showPopup.value = false
                    }
                    true
                }
        } else {
            Modifier
                .fillMaxSize()
        }
        LazyColumn(modifier, state) {
            itemsIndexed(folders) { index, folder ->
                Folder(folder) {
                    onFolderClicked(index)
                }
            }
        }
        if (state.isScrollInProgress) {
            onScroll(calcScroll(state))
        }
    }
}

@Composable
fun Folder(folder: Folder, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10))
            .background(CreamWhite)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.padding(8.dp)) {
            Image(
                ImageVector.vectorResource(R.drawable.ic_folder),
                "",
                Modifier.size(48.dp)
            )
        }
        Column(
            Modifier.padding(start = 12.dp, end = 12.dp)
        ) {
            Text(
                folder.name
            )
            Text(
                folder.numberOfSongs.toString(),
                Modifier.padding(start = 5.dp)
            )
        }
    }
}

package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R

@Composable
fun BoxScope.ScreenOptionsPopup(
    isVisible: Boolean,
    onToggle: () -> Unit,
    onSongCategoryChanged: (String) -> Unit
) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopCenter),
        visible = isVisible,
        enter = fadeIn(tween(500)),
        exit = slideOutVertically(
            tween(1000)
        ) {
            -it
        }
    ) {
        Column(
            Modifier
                .wrapContentSize()
                .background(Color.LightGray, RoundedCornerShape(10)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Category(
                stringResource(R.string.all_songs)
            ) {
                onSongCategoryChanged(it)
                onToggle()
            }
            Category(
                stringResource(R.string.artists)
            ) {
                onSongCategoryChanged(it)
                onToggle()
            }
            Category(
                stringResource(R.string.albums)
            ) {
                onSongCategoryChanged(it)
                onToggle()
            }
            Category(
                stringResource(R.string.folders)
            ) {
                onSongCategoryChanged(it)
                onToggle()
            }
        }
    }
}

@Composable
fun Category(
    name: String,
    selectSongsCategory: (String) -> Unit
) {
    Text(
        name,
        modifier = Modifier
            .clickable {
                selectSongsCategory(name)
            }
            .padding(top = 12.dp, bottom = 12.dp, start = 48.dp, end = 48.dp)
    )
}

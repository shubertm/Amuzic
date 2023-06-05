package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.TopBar(
    title: String,
    isVisible: Boolean,
    showCategoryPopup: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        Modifier.align(Alignment.TopCenter),
        enter = slideInVertically(tween(1000)) {
            -it
        },
        exit = slideOutVertically(tween(500)) {
            -it
        }
    ) {
        Column(
            Modifier
                .background(Color.White)
                .fillMaxWidth()
                .border(0.dp, Color.LightGray)
                .wrapContentHeight()
                .clickable {},
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        MaterialTheme.colors.primary,
                        RoundedCornerShape(
                            bottomStartPercent = 30,
                            bottomEndPercent = 30
                        )
                    )
                    .padding(
                        top = 12.dp,
                        bottom = 12.dp,
                        start = 32.dp,
                        end = 32.dp
                    ),
                color = Color.White
            )
            Box(
                Modifier
                    .wrapContentSize()
                    .clickable {
                        showCategoryPopup()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    modifier = Modifier.size(32.dp),
                    contentDescription = ""
                )
            }
        }
    }
}

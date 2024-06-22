package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.infbyte.amuzic.R
import com.infbyte.amuzic.ui.theme.AmuzicTheme
import com.infbyte.amuzic.ui.viewmodel.AmuzicState

@Composable
fun BoxScope.PlayBar(
    isVisible: State<Boolean>,
    state: AmuzicState,
    playbackMode: AmuzicState,
    onPlayClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onTogglePlaybackMode: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onShowPlayListClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible.value,
        Modifier
            .align(Alignment.BottomCenter),
        enter = expandVertically(tween(1000), Alignment.Bottom),
        exit = shrinkVertically(tween(1000), Alignment.Top)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStartPercent = 20, topEndPercent = 20))
                .clickable {}
                .border(0.dp, Color.LightGray)
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStartPercent = 20, topEndPercent = 20)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        onTogglePlaybackMode()
                    },
                    Modifier
                        .padding(16.dp)
                        .clip(CircleShape)

                ) {
                    Icon(
                        when {
                            playbackMode.mode == Player.REPEAT_MODE_ONE ->
                                painterResource(R.drawable.ic_repeat_one)
                            playbackMode.mode == Player.REPEAT_MODE_ALL ->
                                painterResource(R.drawable.ic_repeat)
                            playbackMode.shuffle && playbackMode.mode == Player.REPEAT_MODE_OFF ->
                                painterResource(R.drawable.ic_shuffle)
                            else -> painterResource(R.drawable.ic_repeat)
                        },
                        "",
                        Modifier.size(32.dp),
                        tint = if (!playbackMode.shuffle && playbackMode.mode == Player.REPEAT_MODE_OFF) {
                            Color.LightGray
                        } else {
                            Color.Black
                        }
                    )
                }
                IconButton(
                    onClick = { onPrevClick() },
                    Modifier.clip(CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_skip_previous),
                        "",
                        Modifier.size(32.dp)
                    )
                }
                Box(
                    Modifier
                        .clip(CircleShape)
                        .size(58.dp)
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state.isPlaying) {
                            ImageVector.vectorResource(R.drawable.ic_pause)
                        } else { Icons.Outlined.PlayArrow },
                        "",
                        Modifier.size(52.dp)
                    )
                }
                IconButton(
                    onClick = { onNextClick() },
                    Modifier.clip(CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_skip_next),
                        "",
                        Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = { onShowPlayListClick() },
                    Modifier
                        .padding(16.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_queue_music),
                        "",
                        Modifier.size(32.dp)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.currentSong.title,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Slider(
                        value = state.progress,
                        onValueChange = { onSeekTo(it) },
                        modifier = Modifier
                            .padding(
                                start = 32.dp,
                                end = 32.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .height(32.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPlayBar() {
    AmuzicTheme {
        Box {
            PlayBar(
                isVisible = remember {
                    mutableStateOf(true)
                },
                state = remember {
                    AmuzicState.INITIAL_STATE
                },
                remember {
                    AmuzicState.INITIAL_STATE
                },
                onPlayClick = {},
                onNextClick = {},
                onPrevClick = {},
                onTogglePlaybackMode = {},
                onSeekTo = {},
                onShowPlayListClick = {}
            )
        }
    }
}

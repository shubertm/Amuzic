package com.infbyte.amuzic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.infbyte.amuzic.R
import com.infbyte.amuzic.data.model.Song
import com.infbyte.amuzic.playback.PlaybackMode

@Composable
fun BoxScope.PlayBar(
    isVisible: State<Boolean>,
    isPlaying: State<Boolean>,
    song: Song,
    progress: State<Float>,
    playbackMode: State<PlaybackMode>,
    onPlayClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onTogglePlaybackMode: () -> Unit
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
                    MaterialTheme.colors.background,
                    RoundedCornerShape(topStartPercent = 20, topEndPercent = 20)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (playbackMode.value) {
                        PlaybackMode.REPEAT_ONE ->
                            painterResource(R.drawable.ic_repeat_one)
                        PlaybackMode.REPEAT_ALL -> painterResource(R.drawable.ic_repeat)
                        PlaybackMode.SHUFFLE -> painterResource(R.drawable.ic_shuffle)
                    },
                    "",
                    Modifier.clickable { onTogglePlaybackMode() }
                )
                Icon(
                    painterResource(R.drawable.ic_skip_previous),
                    "",
                    Modifier
                        .background(MaterialTheme.colors.background, CircleShape)
                        .clip(CircleShape)
                        .border(0.dp, Color.DarkGray)
                        .size(32.dp)
                        .clickable { onPrevClick() },
                    tint = MaterialTheme.colors.primary
                )
                Box(
                    Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .wrapContentSize()
                ) {
                    CircularProgressIndicator(
                        progress.value,
                        Modifier.size(48.dp),
                        strokeWidth = 1.5.dp
                    )
                    Icon(
                        if (isPlaying.value) {
                            ImageVector.vectorResource(R.drawable.ic_pause)
                        } else { Icons.Filled.PlayArrow },
                        "",
                        Modifier
                            .clip(CircleShape)
                            .size(48.dp)
                            .clickable {
                                onPlayClick()
                            }
                    )
                }
                Icon(
                    painterResource(R.drawable.ic_skip_next),
                    "",
                    Modifier
                        .background(MaterialTheme.colors.background, CircleShape)
                        .clip(CircleShape)
                        .border(0.dp, Color.DarkGray)
                        .size(32.dp)
                        .clickable { onNextClick() },
                    tint = MaterialTheme.colors.primary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .height(32.dp)
                        .background(
                            MaterialTheme.colors.primary,
                            RoundedCornerShape(20)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        song.name,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

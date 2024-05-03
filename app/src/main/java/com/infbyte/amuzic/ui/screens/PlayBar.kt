package com.infbyte.amuzic.ui.screens

import android.content.res.ColorStateList
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    onTogglePlaybackMode: () -> Unit,
    onSeekTo: (Float) -> Unit
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
                Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onTogglePlaybackMode()
                    },
                    Modifier.padding(16.dp).background(
                        MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(
                        when (playbackMode.value) {
                            PlaybackMode.REPEAT_ONE ->
                                painterResource(R.drawable.ic_repeat_one)

                            PlaybackMode.REPEAT_ALL ->
                                painterResource(R.drawable.ic_repeat)

                            PlaybackMode.SHUFFLE ->
                                painterResource(R.drawable.ic_shuffle)
                        },
                        "",
                        Modifier
                            .size(32.dp)
                    )
                }
                IconButton(
                    onClick = { onPrevClick() },
                    Modifier.background(Color.Gray, CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_skip_previous),
                        "",
                        Modifier.size(32.dp)
                    )
                }
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
                IconButton(
                    onClick = { onNextClick() },
                    Modifier.background(Color.Gray, CircleShape)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_skip_next),
                        "",
                        Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = {},
                    Modifier.padding(16.dp).background(
                        MaterialTheme.colorScheme.background,
                        CircleShape
                    )
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
                    Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        song.name,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Slider(
                        value = progress.value,
                        onValueChange = { onSeekTo(it) },
                        modifier = Modifier.padding(
                            start = 32.dp,
                            end = 32.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ).height(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeekBar(
    progress: Float,
    height: Dp,
    color: Color,
    onSeekTo: (Float) -> Unit
) {
    AndroidView(
        factory = { context ->
            val seekBar = SeekBar(context)
            val seekListener = object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    position: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        onSeekTo(position.toFloat() / 1000)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
            seekBar.max = 1000
            seekBar.progressTintList = ColorStateList.valueOf(color.toArgb())
            seekBar.thumbTintList = ColorStateList.valueOf(color.toArgb())
            seekBar.setOnSeekBarChangeListener(seekListener)
            seekBar
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        update = { seekBar ->
            seekBar.progress = (progress * 1000).toInt()
        }
    )
}

package com.infbyte.amuzic.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlaybackManager @Inject constructor(
    @ApplicationContext context: Context,
    private val playbackListener: PlaybackListener
) {
    private var hasAudioFocus = false
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE)
        as AudioManager
    private val audioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                }

                AudioManager.AUDIOFOCUS_LOSS -> pauseSong()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val focusRequest = AudioFocusRequest.Builder(
        AudioManager.AUDIOFOCUS_GAIN
    )
        .apply {
            setAudioAttributes(
                AudioAttributes.Builder().apply {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                }.build()
            )
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener(audioFocusChangeListener)
        }.build()

    fun requestAudioFocus(isGranted: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val response = audioManager.requestAudioFocus(focusRequest)
            val lock = "audio_focus"
            synchronized(lock) {
                when (response) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        hasAudioFocus = true
                    }

                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {}

                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                        hasAudioFocus = false
                    }
                }
                isGranted(hasAudioFocus)
            }
        }
    }

    fun pauseSong() {
        playbackListener.pauseSong()
        abandonAudioFocus()
        hasAudioFocus = false
        checkPlayer()
    }

    fun checkPlayer() {
        _isPlaying.value = playbackListener.isActive()
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }
}

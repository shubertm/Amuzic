package com.infbyte.amuzic.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PlaybackManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val audioFocusChangeListener: OnAudioFocusChangeListener
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE)
        as AudioManager

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
        }
        .build()

    fun requestAudioFocus(isGranted: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val response = audioManager.requestAudioFocus(focusRequest)
            val lock = "audio_focus"
            synchronized(lock) {
                when (response) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        isGranted(true)
                    }

                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {}

                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                        isGranted(false)
                    }
                }
            }
        }
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted context: Context,
            @Assisted audioFocusChangeListener: OnAudioFocusChangeListener
        ): PlaybackManager
    }
}

package com.infbyte.amuzic.playback

import android.content.Context
import com.infbyte.amuzic.data.model.Song

interface PlaybackListener {

    fun init(context: Context, onPrepared: () -> Unit, onComplete: () -> Unit)

    fun initSong(song: Song)

    fun prepareSong(song: Song)

    fun playSong()

    fun pauseSong()

    fun stopSong()

    fun seekTo(position: Float)

    fun progress(): Int

    fun duration(): Int

    fun isActive(): Boolean

    fun releasePlayer()
}

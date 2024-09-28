package com.infbyte.amuzic.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.RepeatMode

interface AmuzicPlayer {

    val shuffleMode: Boolean

    val currentSong: MediaItem

    var onTransition: (Int, Float) -> Unit

    suspend fun init()

    fun initController(context: Context)

    fun releaseControllerFuture()

    fun selectSong(index: Int)

    fun playSong()

    fun pauseSong()

    fun stopSong()

    fun nextSong()

    fun prevSong()

    fun seekTo(position: Float)

    fun progress(): Float

    fun duration(): Float

    fun isActive(): Boolean

    fun releasePlayer()

    fun addToPlayList(songs: List<MediaItem>)

    fun createPlayList(songs: List<MediaItem>)

    @RepeatMode
    fun switchMode(): Int
}

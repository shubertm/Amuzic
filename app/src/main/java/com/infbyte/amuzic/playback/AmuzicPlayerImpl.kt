package com.infbyte.amuzic.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import javax.inject.Inject

class AmuzicPlayerImpl @Inject constructor() : AmuzicPlayer {

    private var mediaController: MediaController? = null

    override val shuffleMode: Boolean get() = mediaController?.run { shuffleModeEnabled }!!

    override val currentSong: MediaItem get() = mediaController?.run { currentMediaItem }!!

    override var onTransition: (Int, Float) -> Unit = { _, _ -> }

    private val listener: Player.Listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            mediaController?.run {
                if (mediaItem != null) {
                    onTransition(currentMediaItemIndex, duration.toFloat())
                }
            }
        }
    }

    override fun init(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, AmuzicPlayerService::class.java))
        mediaController = MediaController.Builder(context, sessionToken).buildAsync().get()
        mediaController?.run {
            addListener(listener)
        }
    }

    override fun createPlayList(songs: List<MediaItem>) {
        mediaController?.run { setMediaItems(songs) }
    }

    override fun addToPlayList(songs: List<MediaItem>) {
        mediaController?.run { addMediaItems(songs) }
    }

    @Player.RepeatMode
    override fun switchMode(): Int = mediaController?.run {
        when {
            repeatMode == Player.REPEAT_MODE_ONE -> {
                repeatMode = Player.REPEAT_MODE_ALL
            }
            repeatMode == Player.REPEAT_MODE_ALL -> {
                repeatMode = Player.REPEAT_MODE_OFF
            }
            !shuffleModeEnabled && repeatMode == Player.REPEAT_MODE_OFF -> {
                shuffleModeEnabled = true
            }
            shuffleModeEnabled && repeatMode == Player.REPEAT_MODE_OFF -> {
                repeatMode = Player.REPEAT_MODE_ONE
                shuffleModeEnabled = false
            }
        }
        repeatMode
    }!!

    override fun selectSong(index: Int) {
        mediaController?.run { seekTo(index, 0) }
    }

    override fun playSong() {
        mediaController?.run { play() }
    }

    override fun pauseSong() {
        mediaController?.run { pause() }
    }

    override fun stopSong() {
        mediaController?.run { stop() }
    }

    override fun nextSong() {
        mediaController?.run { seekToNextMediaItem() }
    }

    override fun prevSong() {
        mediaController?.run { seekToPreviousMediaItem() }
    }

    override fun seekTo(position: Float) {
        mediaController?.run {
            val pos = (position * duration).toLong()
            this.seekTo(pos)
        }
    }

    override fun progress(): Float = mediaController?.run { currentPosition.toFloat() } ?: 0f

    override fun duration(): Float = mediaController?.run { duration.toFloat() } ?: 0f

    override fun isActive() = mediaController?.run { isPlaying } ?: false

    override fun releasePlayer() {
        mediaController?.run {
            stop()
            release()
            removeListener(listener)
        }
        mediaController = null
    }
}

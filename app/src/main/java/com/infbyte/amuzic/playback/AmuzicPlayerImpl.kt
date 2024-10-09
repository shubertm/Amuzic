package com.infbyte.amuzic.playback

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.inject.Inject

class AmuzicPlayerImpl @Inject constructor() : AmuzicPlayer {

    private var mediaController: MediaController? = null
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    override val shuffleMode: Boolean get() = mediaController?.run { shuffleModeEnabled } ?: false

    override val currentSong: MediaItem get() = mediaController?.run { currentMediaItem } ?: MediaItem.EMPTY

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

    override fun initController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, AmuzicPlayerService::class.java))

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture.addListener(
            {
                try {
                    mediaController = mediaControllerFuture.get()
                    mediaController?.addListener(listener)
                    mediaController?.prepare()
                } catch (e: CancellationException) {
                    Log.d(LOG_TAG, "Failed to initialize media controller, initialization was cancelled")
                } catch (e: ExecutionException) {
                    Log.d(LOG_TAG, "Failed to initialize media controller with exception ${e.printStackTrace()}")
                } catch (e: InterruptedException) {
                    Log.d(LOG_TAG, "Failed to initialize media controller, initialization was interrupted")
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    override fun releaseControllerFuture() {
        mediaController?.removeListener(listener)
        MediaController.releaseFuture(mediaControllerFuture)
        mediaController = null
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
    } ?: Player.REPEAT_MODE_OFF

    override fun selectSong(index: Int, position: Float) {
        mediaController?.run {
            val pos = (position * duration).toLong()
            seekTo(index, pos)
        }
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
            seekTo(pos)
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

    companion object {
        private const val LOG_TAG = "Amuzic Player"
    }
}

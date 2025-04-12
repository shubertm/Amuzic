package com.infbyte.amuzic.playback

import android.content.ComponentName
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlin.concurrent.timer

class AmuzicPlayerImpl @Inject constructor() : AmuzicPlayer {

    private var mediaController: MediaController? = null
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    override val shuffleMode: Boolean get() = mediaController?.run { shuffleModeEnabled } ?: false

    override val currentSong: MediaItem get() = mediaController?.run { currentMediaItem } ?: MediaItem.EMPTY

    override var onTransition: (Int, Float) -> Unit = { _, _ -> }

    override var sendIsPlaying: (Boolean) -> Unit = { _ -> }

    override var sendProgress: (Float) -> Unit = { _ -> }

    private var timer: Timer? = null

    private val mediaControllerFutureListener: Runnable = Runnable {
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
    }

    private val listener: Player.Listener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            sendIsPlaying(isPlaying)
            startTimer()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            mediaController?.run {
                if (mediaItem != null) {
                    onTransition(currentMediaItemIndex, duration.toFloat())
                    sendProgress(0f)
                }
            }
        }
    }

    override fun initController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, AmuzicPlayerService::class.java))

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        mediaControllerFuture.addListener(
            mediaControllerFutureListener,
            ContextCompat.getMainExecutor(context)
        )
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

    override fun selectSong(index: Int, position: Long) {
        mediaController?.run { seekTo(index, position) }
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

    override fun isActive(): Boolean = mediaController?.run { isPlaying } ?: false

    override fun releasePlayer() {
        mediaController?.run {
            stop()
            release()
            removeListener(listener)
        }
        releaseControllerFuture()
        mediaController = null
    }

    override fun areSongsAvailable(): Boolean {
        return mediaController?.run { mediaItemCount > 0 } ?: false
    }

    private fun startTimer() {
        stopTimer()
        timer = timer(startAt = Calendar.getInstance().time, period = 10L) {
            runBlocking {
                launch(Dispatchers.Main) {
                    val progress = progress().coerceAtLeast(0f) / duration()
                    if (progress.isNaN()) {
                        return@launch
                    }
                    sendProgress(progress)
                    if (!isActive()) {
                        stopTimer()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    private fun releaseControllerFuture() {
        MediaController.releaseFuture(mediaControllerFuture)
    }

    companion object {
        private const val LOG_TAG = "Amuzic Player"
    }
}

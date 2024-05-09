package com.infbyte.amuzic.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.infbyte.amuzic.data.model.Song
import javax.inject.Inject

class AmuzicPlayer @Inject constructor() :
    MediaPlayer(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener,
    PlaybackListener {
    private var isInitSong = true
    private lateinit var context: Context
    private lateinit var onPreparedHandler: () -> Unit
    private lateinit var onCompletionHandler: () -> Unit

    override fun init(
        context: Context,
        onPrepared: () -> Unit,
        onComplete: () -> Unit
    ) {
        this.context = context
        onPreparedHandler = onPrepared
        onCompletionHandler = onComplete
        setOnPreparedListener(this)
        setOnCompletionListener(this)
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }

    override fun initSong(song: Song) {
        isInitSong = true
        setDataSource(context, song.uri)
        prepare()
    }

    override fun prepareSong(song: Song) {
        reset()
        setDataSource(context, song.uri)
        prepare()
    }

    override fun playSong() {
        start()
    }

    override fun pauseSong() {
        pause()
    }

    override fun stopSong() {
        stop()
    }

    override fun seekTo(position: Float) {
        val pos = (position * duration).toInt()
        super.seekTo(pos)
    }

    override fun progress() = currentPosition

    override fun duration() = duration

    override fun isActive() = this.isPlaying

    override fun release() {
        release()
    }

    override fun onCompletion(player: MediaPlayer?) {
        onCompletionHandler()
    }

    override fun onPrepared(player: MediaPlayer?) {
        if (!isInitSong) onPreparedHandler()
        isInitSong = false
    }

    override fun onSeekComplete(player: MediaPlayer?) {}
}

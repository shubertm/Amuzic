package com.infbyte.amuzic.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.infbyte.amuzic.data.model.Song
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Inject

class AmuzicPlayer @Inject constructor() :
    MediaPlayer(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
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

    override fun progress() = currentPosition

    override fun duration() = duration

    override fun isActive() = this.isPlaying

    override fun onCompletion(p0: MediaPlayer?) {
        onCompletionHandler()
    }

    override fun onPrepared(p0: MediaPlayer?) {
        if (!isInitSong) onPreparedHandler()
        isInitSong = false
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AmuzicPlayerModule() {
    @Binds
    abstract fun bindPlaybackListener(
        player: AmuzicPlayer
    ): PlaybackListener
}

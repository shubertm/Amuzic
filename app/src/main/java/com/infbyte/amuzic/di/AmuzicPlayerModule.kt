package com.infbyte.amuzic.di

import android.content.Context
import com.infbyte.amuzic.playback.AmuzicPlayer
import com.infbyte.amuzic.playback.PlaybackListener
import com.infbyte.amuzic.playback.PlaybackManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AmuzicPlayerModule() {
    @Binds
    @Singleton
    abstract fun bindPlaybackListener(
        player: AmuzicPlayer
    ): PlaybackListener
}

@Module
@InstallIn(ViewModelComponent::class)
object PlayerManagerModule {
    @Provides
    fun providePlaybackManager(
        @ApplicationContext context: Context,
        playbackListener: PlaybackListener
    ) = PlaybackManager(context, playbackListener)
}

package com.infbyte.amuzic.di

import com.infbyte.amuzic.playback.AmuzicPlayer
import com.infbyte.amuzic.playback.AmuzicPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class AmuzicPlayerModule {
    @Binds
    abstract fun bindPlaybackListener(player: AmuzicPlayerImpl): AmuzicPlayer
}

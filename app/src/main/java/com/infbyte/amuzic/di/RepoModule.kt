package com.infbyte.amuzic.di

import android.content.Context
import com.infbyte.amuzic.data.repo.SongsRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewModelComponent::class)
object RepoModule {
    @Provides
    fun provideSongsRepo(
        @ApplicationContext context: Context,
    ) = SongsRepo(context)
}

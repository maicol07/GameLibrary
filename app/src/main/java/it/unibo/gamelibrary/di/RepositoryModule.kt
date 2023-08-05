package it.unibo.gamelibrary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.unibo.gamelibrary.GameLibraryApplication
import it.unibo.gamelibrary.data.repository.FollowRepository
import it.unibo.gamelibrary.data.repository.LibraryRepository
import it.unibo.gamelibrary.data.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Singleton
    @Provides
    fun bindUserRepository(@ApplicationContext context: Context) = UserRepository(
        (context.applicationContext as GameLibraryApplication).database.userDao()
    )
    @Singleton
    @Provides
    fun bindLibraryRepository(@ApplicationContext context: Context) = LibraryRepository(
        (context.applicationContext as GameLibraryApplication).database.libraryDao()
    )
    @Singleton
    @Provides
    fun bindFollowRepository(@ApplicationContext context: Context) = FollowRepository(
        (context.applicationContext as GameLibraryApplication).database.followDao()
    )
}
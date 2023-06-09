package it.unibo.gamelibrary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.unibo.gamelibrary.GameLibraryApplication
import it.unibo.gamelibrary.data.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Singleton
    @Provides
    fun bindRepository(@ApplicationContext context: Context) = UserRepository(
        (context.applicationContext as GameLibraryApplication).database.userDao()
    )
}
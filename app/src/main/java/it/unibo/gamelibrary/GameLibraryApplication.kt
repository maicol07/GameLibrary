package it.unibo.gamelibrary

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import it.unibo.gamelibrary.data.GameLibraryDatabase

@HiltAndroidApp
class GameLibraryApplication : Application() {
    val database by lazy { GameLibraryDatabase.getDatabase(this) }
}
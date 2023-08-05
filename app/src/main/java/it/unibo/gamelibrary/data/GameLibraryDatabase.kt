package it.unibo.gamelibrary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.unibo.gamelibrary.data.dao.LibraryDao
import it.unibo.gamelibrary.data.dao.UserDao
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.User

@Database(entities = [User::class], version = 2)
        LibraryEntry::class,
abstract class GameLibraryDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun libraryDao(): LibraryDao

    companion object{
        @Volatile
        private var INSTANCE : GameLibraryDatabase ?= null

        fun getDatabase(context: Context): GameLibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameLibraryDatabase::class.java,
                    "game_library"
                ).build()
                INSTANCE = instance

                instance
            }
        }
    }
}
package it.unibo.gamelibrary.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.unibo.gamelibrary.data.dao.FollowDao
import it.unibo.gamelibrary.data.dao.LibraryDao
import it.unibo.gamelibrary.data.dao.UserDao
import it.unibo.gamelibrary.data.model.Follow
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.User

@Database(
    entities = [
        User::class,
        LibraryEntry::class,
        Follow::class
   ],
    autoMigrations = [
        AutoMigration (from = 8, to = 9)
    ],
    version = 9
)
abstract class GameLibraryDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun libraryDao(): LibraryDao
    abstract fun followDao(): FollowDao

    companion object {
        @Volatile
        private var INSTANCE: GameLibraryDatabase? = null

        fun getDatabase(context: Context): GameLibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameLibraryDatabase::class.java,
                    "game_library"
                )
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}
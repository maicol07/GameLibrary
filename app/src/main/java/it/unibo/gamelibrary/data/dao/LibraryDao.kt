package it.unibo.gamelibrary.data.dao

import androidx.room.*
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.UserWithLibraryEntries

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library")
    suspend fun getAll(): List<LibraryEntry>

    @Query("SELECT * FROM library WHERE uid = :userId")
    suspend fun getUserLibrary(userId: String): List<LibraryEntry>

    @Query("SELECT * FROM library WHERE id = :id")
    suspend fun getLibrary(id: String): LibraryEntry?

    @Query("SELECT * FROM library WHERE gameId = :gameId")
    suspend fun getLibraryEntriesByGame(gameId: String): List<LibraryEntry>

    @Insert
    suspend fun add(libraryEntry: LibraryEntry)

    @Update
    suspend fun update(libraryEntry: LibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LibraryEntry)

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getUsersAndLibrary(): List<UserWithLibraryEntries>

    // Get library entry by user id and game id
    @Query("SELECT * FROM library WHERE uid = :userId AND gameId = :gameId")
    suspend fun getLibraryEntryByUserAndGame(userId: String, gameId: String): LibraryEntry?
}
package it.unibo.gamelibrary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import it.unibo.gamelibrary.data.model.LibraryEntry
import it.unibo.gamelibrary.data.model.UserWithLibraryEntries
import kotlinx.coroutines.flow.Flow
import ru.pixnews.igdbclient.model.Game

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library")
    fun getAll(): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE uid = :userId")
    fun getUserLibrary(userId: String): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE id = :id")
    fun getLibrary(id: String): Flow<LibraryEntry?>

    @Query("SELECT * FROM library WHERE gameId = :gameId")
    fun getLibraryEntriesByGame(gameId: String): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE gameId IN (:games)")
    fun getLibraryEntriesByGames(games: List<Long>): Flow<List<LibraryEntry>>

    @Insert
    suspend fun add(libraryEntry: LibraryEntry)

    @Update
    suspend fun update(libraryEntry: LibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LibraryEntry)

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersAndLibrary(): Flow<List<UserWithLibraryEntries>>

    // Get library entry by user id and game id
    @Query("SELECT * FROM library WHERE uid = :userId AND gameId = :gameId")
    fun getLibraryEntryByUserAndGame(userId: String, gameId: String): Flow<LibraryEntry?>
}
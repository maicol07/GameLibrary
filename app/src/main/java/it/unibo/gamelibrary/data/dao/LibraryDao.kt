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

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library ORDER BY :orderBy")
    fun getAll(orderBy: String): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE uid = :userId ORDER BY :orderBy")
    fun getUserLibrary(userId: String, orderBy: String): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE id = :id")
    fun getLibrary(id: String): Flow<LibraryEntry?>

    @Query("SELECT * FROM library WHERE gameId = :gameId ORDER BY :orderBy")
    fun getLibraryEntriesByGame(gameId: String, orderBy: String): Flow<List<LibraryEntry>>

    @Query("SELECT * FROM library WHERE gameId IN (:games) ORDER BY :orderBy")
    fun getLibraryEntriesByGames(games: List<Long>, orderBy: String): Flow<List<LibraryEntry>>

    @Insert
    suspend fun add(libraryEntry: LibraryEntry)

    @Update
    suspend fun update(libraryEntry: LibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LibraryEntry)

    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersAndLibrary(): Flow<List<UserWithLibraryEntries>>

    @Query("SELECT * FROM library WHERE uid = :userId AND gameId = :gameId")
    fun getLibraryEntryByUserAndGame(userId: String, gameId: String): Flow<LibraryEntry?>
}
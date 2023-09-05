package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.LibraryDao
import it.unibo.gamelibrary.data.model.LibraryEntry
import kotlinx.coroutines.flow.Flow
import ru.pixnews.igdbclient.model.Game

class LibraryRepository(private val libraryDao: LibraryDao) {
    @WorkerThread
    fun getAll(orderBy: String = "id"): Flow<List<LibraryEntry>> = libraryDao.getAll(orderBy)

    @WorkerThread
    fun getLibraryEntriesByGame(game: Game, sortBy: String = "id"): Flow<List<LibraryEntry>> =
        libraryDao.getLibraryEntriesByGame(game.id.toString(), sortBy)

    @WorkerThread
    fun getLibraryEntriesByGames(games: List<Game>, orderBy: String = "id"): Flow<List<LibraryEntry>> =
        libraryDao.getLibraryEntriesByGames(games.map { it.id }, orderBy)

    @WorkerThread
    fun getUserLibraryEntries(uid: String, orderBy: String = "id"): Flow<List<LibraryEntry>> =
        libraryDao.getUserLibrary(uid, orderBy)

    @WorkerThread
    fun getCollection(id: String): Flow<LibraryEntry?> = libraryDao.getLibrary(id)

    @WorkerThread
    suspend fun insertEntry(libraryEntry: LibraryEntry) {
        libraryDao.add(libraryEntry)
    }

    @WorkerThread
    suspend fun updateEntry(libraryEntry: LibraryEntry) {
        libraryDao.update(libraryEntry)
    }

    @WorkerThread
    suspend fun deleteEntry(libraryEntry: LibraryEntry) {
        libraryDao.delete(libraryEntry)
    }

    @WorkerThread
    fun getLibraryEntryByUserAndGame(userId: String, gameId: String): Flow<LibraryEntry?> =
        libraryDao.getLibraryEntryByUserAndGame(userId, gameId)
}
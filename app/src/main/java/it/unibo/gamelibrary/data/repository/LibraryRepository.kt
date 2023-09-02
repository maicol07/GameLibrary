package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.LibraryDao
import it.unibo.gamelibrary.data.model.LibraryEntry
import kotlinx.coroutines.flow.Flow
import ru.pixnews.igdbclient.model.Game

class LibraryRepository(private val libraryDao: LibraryDao) {
    //val allUser: List<User> = collectionDao.getAll()
    @WorkerThread
    fun getAll(): Flow<List<LibraryEntry>> = libraryDao.getAll()

    @WorkerThread
    fun getLibraryEntriesByGame(game: Game): Flow<List<LibraryEntry>> =
        libraryDao.getLibraryEntriesByGame(game.id.toString())

    @WorkerThread
    fun getLibraryEntriesByGames(games: List<Game>): Flow<List<LibraryEntry>> =
        libraryDao.getLibraryEntriesByGames(games.map { it.id })

    @WorkerThread
    fun getUserLibraryEntries(uid: String): Flow<List<LibraryEntry>> =
        libraryDao.getUserLibrary(uid)

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
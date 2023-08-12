package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.LibraryDao
import it.unibo.gamelibrary.data.model.LibraryEntry
import proto.Game

class LibraryRepository(private val libraryDao: LibraryDao) {
    //val allUser: List<User> = collectionDao.getAll()
    @WorkerThread
    suspend fun getAll(): List<LibraryEntry> = libraryDao.getAll()

    @WorkerThread
    suspend fun getCollectionsByGame(game: Game): List<LibraryEntry> {
        return libraryDao.getLibraryEntriesByGame(game.id.toString())
    }

    @WorkerThread
    suspend fun getUserLibraryEntries(uid : String): List<LibraryEntry> {
        return libraryDao.getUserLibrary(uid)
    }

    @WorkerThread
    suspend fun getCollection(id: String): LibraryEntry? {
        return libraryDao.getLibrary(id)
    }

    @WorkerThread
    suspend fun insertEntry(libraryEntry: LibraryEntry){
        libraryDao.add(libraryEntry)
    }

    @WorkerThread
    suspend fun updateEntry(libraryEntry: LibraryEntry){
        libraryDao.update(libraryEntry)
    }

    @WorkerThread
    suspend fun deleteEntry(libraryEntry: LibraryEntry){
        libraryDao.delete(libraryEntry)
    }

    @WorkerThread
    suspend fun getLibraryEntryByUserAndGame(userId: String, gameId: String): LibraryEntry? {
        return libraryDao.getLibraryEntryByUserAndGame(userId, gameId)
    }
}
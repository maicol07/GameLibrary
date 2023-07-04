package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.UserDao
import it.unibo.gamelibrary.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val allUser: Flow<List<User>> = userDao.getAll()

    fun getUserByUsername(username : String): User {
        return userDao.getUserByUsername(username)
    }

    @WorkerThread
    suspend fun insertUser(user: User){
        userDao.insertUser(user)
    }

    @WorkerThread
    suspend fun deleteUser(user: User){
        userDao.delete(user)
    }
}
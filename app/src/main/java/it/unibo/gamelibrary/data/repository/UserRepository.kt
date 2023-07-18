package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.UserDao
import it.unibo.gamelibrary.data.model.User

class UserRepository(private val userDao: UserDao) {
    //val allUser: List<User> = userDao.getAll()
    @WorkerThread
    suspend fun getAll(): List<User>{
        return userDao.getAll()
    }

    @WorkerThread
    suspend fun getUserByUsername(username : String): User? {
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
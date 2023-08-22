package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.UserDao
import it.unibo.gamelibrary.data.model.User

class UserRepository(private val userDao: UserDao) {
    //val allUser: List<User> = userDao.getAll()
    @WorkerThread
    suspend fun getAll(): List<User> {
        return userDao.getAll()
    }

    @WorkerThread
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    @WorkerThread
    suspend fun getUserByUid(uid: String): User? {
        return userDao.getUserByUid(uid)
    }

    @WorkerThread
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    @WorkerThread
    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }

    @WorkerThread
    suspend fun setEmail(uid: String, email: String) {
        userDao.setEmail(uid, email)
    }

    @WorkerThread
    suspend fun setLocation(uid: String, location: String) {
        userDao.setLocation(uid, location)
    }

    @WorkerThread
    suspend fun setImage(uid: String, image: String) {
        userDao.setImage(uid, image)
    }

    @WorkerThread
    suspend fun setUsername(uid: String, username: String) {
        userDao.setUsername(uid, username)
    }

    @WorkerThread
    suspend fun setBio(uid: String, bio: String) {
        userDao.setBio(uid, bio)
    }

    @WorkerThread
    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    @WorkerThread
    suspend fun searchUser(query: String): List<User> {
        return userDao.searchUser(query)
    }
}
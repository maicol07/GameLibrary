package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.FollowDao
import it.unibo.gamelibrary.data.model.Follow

class FollowRepository(private val followDao: FollowDao) {

    @WorkerThread
    suspend fun getFollowed(uid: String): List<Follow> {
        return followDao.getFollowed(uid)
    }

    @WorkerThread
    suspend fun getFollowers(uid: String): List<Follow> {
        return followDao.getFollowers(uid)
    }

    @WorkerThread
    suspend fun follow(uid1: String, uid2: String) {
        followDao.follow(uid1, uid2)
    }

    @WorkerThread
    suspend fun unfollow(uid1: String, uid2: String) {
        followDao.unfollow(uid1, uid2)
    }
}
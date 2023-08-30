package it.unibo.gamelibrary.data.repository

import androidx.annotation.WorkerThread
import it.unibo.gamelibrary.data.dao.FollowDao
import it.unibo.gamelibrary.data.model.Follow
import kotlinx.coroutines.flow.Flow

class FollowRepository(private val followDao: FollowDao) {

    @WorkerThread
    fun getFollowed(uid: String): Flow<List<Follow>> = followDao.getFollowed(uid)

    @WorkerThread
    fun getFollowers(uid: String): Flow<List<Follow>> = followDao.getFollowers(uid)

    @WorkerThread
    suspend fun follow(uid1: String, uid2: String) {
        followDao.follow(uid1, uid2)
    }

    @WorkerThread
    suspend fun unfollow(uid1: String, uid2: String) {
        followDao.unfollow(uid1, uid2)
    }
}
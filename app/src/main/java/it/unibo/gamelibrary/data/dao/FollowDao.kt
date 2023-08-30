package it.unibo.gamelibrary.data.dao

import androidx.room.Dao
import androidx.room.Query
import it.unibo.gamelibrary.data.model.Follow
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    //trova i seguiti da userID
    @Query("SELECT * FROM followers WHERE follower = :userId")
    fun getFollowed(userId: String): Flow<List<Follow>>

    //trova i seguaci di userID
    @Query("SELECT * FROM followers WHERE followed = :userId")
    fun getFollowers(userId: String): Flow<List<Follow>>

    //TODO cerca utenti per testare
    @Query("INSERT INTO followers VALUES (:follower, :followed)")
    suspend fun follow(follower: String, followed: String)

    @Query("DELETE FROM followers WHERE follower = :follower AND followed = :followed")
    suspend fun unfollow(follower: String, followed: String)
}
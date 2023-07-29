package it.unibo.gamelibrary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import it.unibo.gamelibrary.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username : String): User?

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid : String): User?

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun delete(user: User)

}
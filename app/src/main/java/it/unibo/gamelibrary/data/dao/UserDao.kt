package it.unibo.gamelibrary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import it.unibo.gamelibrary.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE username = :username")
    fun getUserByUsername(username : String): User

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun delete(user: User)

}
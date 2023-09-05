package it.unibo.gamelibrary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.unibo.gamelibrary.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): Flow<User?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserByUid(uid: String): Flow<User?>

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun delete(user: User)

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET email = :email WHERE uid = :uid")
    suspend fun setEmail(uid: String, email: String)

    @Query("UPDATE users SET address = :address WHERE uid = :uid")
    suspend fun setLocation(uid: String, address: String)

    @Query("UPDATE users SET image = :image WHERE uid = :uid")
    suspend fun setImage(uid: String, image: String)

    @Query("UPDATE users SET username = :username WHERE uid = :uid")
    suspend fun setUsername(uid: String, username: String)

    @Query("UPDATE users SET bio = :bio WHERE uid = :uid")
    suspend fun setBio(uid: String, bio: String)

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR surname LIKE '%' || :query || '%'")
    fun searchUser(query: String): Flow<List<User>>
}
package it.unibo.gamelibrary.data.dao

import androidx.room.*
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
}
package it.unibo.gamelibrary.data.model

import androidx.room.Entity

@Entity(
    primaryKeys = ["follower", "followed"],
    tableName = "followers"
)
data class Follow(
    val follower: String,
    val followed: String
)
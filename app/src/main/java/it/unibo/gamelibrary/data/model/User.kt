package it.unibo.gamelibrary.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["uid"], tableName = "users")
data class User (
    @ColumnInfo var uid: String,
    @ColumnInfo var name: String,
    @ColumnInfo var surname: String,
    @ColumnInfo var username: String,
    @ColumnInfo var email: String,
    @ColumnInfo var address: String? = null
)

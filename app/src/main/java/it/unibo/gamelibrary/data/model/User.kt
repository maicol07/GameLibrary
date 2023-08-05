package it.unibo.gamelibrary.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

data class UserWithLibraryEntries(
    @Embedded val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "uid"
    ) val libraryEntryEntries: List<LibraryEntry>
)

@Entity(primaryKeys = ["uid"], tableName = "users")
data class User (
    @ColumnInfo var uid: String,
    @ColumnInfo var name: String,
    @ColumnInfo var surname: String,
    @ColumnInfo var username: String,
    @ColumnInfo var email: String,
    @ColumnInfo var address: String? = null
)

package it.unibo.gamelibrary.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import java.io.File

data class UserWithLibraryEntries(
    @Embedded val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "uid"
    ) val libraryEntryEntries: List<LibraryEntry>
)

@Entity(primaryKeys = ["uid"], tableName = "users")
data class User(
    @ColumnInfo var uid: String,
    @ColumnInfo var name: String? = null,
    @ColumnInfo var surname: String? = null,
    @ColumnInfo var username: String,
    @ColumnInfo var email: String,
    @ColumnInfo var address: String? = null,
    @ColumnInfo var image: String? = null,
    @ColumnInfo var bio: String? = null,
    @ColumnInfo var isPublisher: Boolean = false,
    @ColumnInfo var publisherName: String? = null
){
    fun hasImage(): Boolean{
        val file = image?.let { File(it) }
        return image != null && image != "" && file?.exists() == true
    }
}


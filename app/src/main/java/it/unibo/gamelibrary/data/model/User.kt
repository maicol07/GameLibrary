package it.unibo.gamelibrary.data.model

import android.net.Uri
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
) {
    fun hasImage(): Boolean {
        if (image != null && Uri.parse(image) != Uri.EMPTY) {
            return Uri.parse(image).path?.let { File(it).exists() } == true
        }
        return false
    }
}


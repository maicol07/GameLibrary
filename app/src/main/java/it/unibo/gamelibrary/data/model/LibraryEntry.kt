package it.unibo.gamelibrary.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LibraryEntryStatus(
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val text: String
) {
    WANTED(Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark, "Wanted"),
    PLAYING(Icons.Outlined.VideogameAsset, Icons.Filled.VideogameAsset, "Playing"),
    FINISHED(Icons.Outlined.Flag, Icons.Filled.Flag, "Finished"),
}

@Entity(
    tableName = "library", foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["uid"]
        )
    ],
    indices = [
        Index(value = ["uid"]),
        Index(value = ["uid", "gameId"], unique = true)
    ]
)
data class LibraryEntry(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo var uid: String,
    @ColumnInfo var gameId: Int,
    @ColumnInfo var status: LibraryEntryStatus,
    @ColumnInfo var rating: Int? = null,
    @ColumnInfo var notes: String? = null,
    @ColumnInfo(name = "last_modified", defaultValue = "0") val lastModified: Long = System.currentTimeMillis()
)

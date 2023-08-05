package it.unibo.gamelibrary.data.model

import androidx.room.*

enum class LibraryEntryStatus {
    WANTED,
    PLAYING,
    FINISHED
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
    @ColumnInfo var status: LibraryEntryStatus? = null,
    @ColumnInfo var rating: Int? = null,
    @ColumnInfo var notes: String? = null
//  @ColumnInfo(name = "created_at") val createdAt: Long, //forse servono per ordinare in ordine NEW!
//  @ColumnInfo(name = "modified_at") val modifiedAt: Long
)

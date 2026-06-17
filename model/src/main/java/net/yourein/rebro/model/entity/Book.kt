package net.yourein.rebro.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.ReadingStatus

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Bookshelf::class,
            parentColumns = ["id"],
            childColumns = ["bookshelf_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("bookshelf_id")],
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "bookshelf_id")
    val bookshelfId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "subtitle")
    val subtitle: String? = null,
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null,
    @ColumnInfo(name = "book_type")
    val bookType: BookType,
    @ColumnInfo(name = "reading_status", defaultValue = "UNREAD")
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
)

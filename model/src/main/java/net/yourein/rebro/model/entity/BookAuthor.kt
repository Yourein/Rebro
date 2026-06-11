package net.yourein.rebro.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "book_authors",
    primaryKeys = ["book_id", "author_id"],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Author::class,
            parentColumns = ["id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BookAuthor(
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    @ColumnInfo(name = "author_id")
    val authorId: Long,
)

package net.yourein.rebro.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "doujin_book_details",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Circle::class,
            parentColumns = ["id"],
            childColumns = ["circle_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("book_id", unique = true),
        Index("circle_id"),
    ],
)
data class DoujinBookDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    @ColumnInfo(name = "circle_id")
    val circleId: Long? = null,
    @ColumnInfo(name = "isdn")
    val isdn: String? = null,
)

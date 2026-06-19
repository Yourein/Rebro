package net.yourein.rebro.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "book_series",
    primaryKeys = ["book_id", "series_id"],
    indices = [Index("series_id")],
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Series::class,
            parentColumns = ["id"],
            childColumns = ["series_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BookSeries(
    @ColumnInfo(name = "book_id")
    val bookId: Long,
    @ColumnInfo(name = "series_id")
    val seriesId: Long,
)

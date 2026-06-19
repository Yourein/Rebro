package net.yourein.rebro.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.BookAuthor
import net.yourein.rebro.model.entity.BookSeries
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.entity.Series

/**
 * 一覧表示に必要な情報を 1 件にまとめた関連モデル。
 *
 * 書籍本体に加え、商業／同人の詳細と著者を同時に取得する。
 * Room は各 [Relation] を `IN` 句のバッチクエリで解決するため、
 * 本の件数によらず一定本数のクエリで完結し N+1 にならない。
 */
data class BookWithDetailAndAuthors(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "id",
        entityColumn = "book_id",
    )
    val commercialDetail: CommercialBookDetail?,
    @Relation(
        entity = DoujinBookDetail::class,
        parentColumn = "id",
        entityColumn = "book_id",
    )
    val doujinDetail: DoujinDetailWithCircle?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookAuthor::class,
            parentColumn = "book_id",
            entityColumn = "author_id",
        )
    )
    val authors: List<Author>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookSeries::class,
            parentColumn = "book_id",
            entityColumn = "series_id",
        )
    )
    val series: List<Series>,
)

package net.yourein.rebro.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail

data class BookWithDetail(
    @Embedded val book: Book,
    @Relation(
        parentColumn = "id",
        entityColumn = "book_id",
    )
    val commercialDetail: CommercialBookDetail?,
    @Relation(
        parentColumn = "id",
        entityColumn = "book_id",
    )
    val doujinDetail: DoujinBookDetail?,
)

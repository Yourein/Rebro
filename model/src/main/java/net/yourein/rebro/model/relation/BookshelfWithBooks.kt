package net.yourein.rebro.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.Bookshelf

data class BookshelfWithBooks(
    @Embedded val bookshelf: Bookshelf,
    @Relation(
        parentColumn = "id",
        entityColumn = "bookshelf_id",
    )
    val books: List<Book>,
)

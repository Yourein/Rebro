package net.yourein.rebro.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.BookAuthor

data class BookWithAuthors(
    @Embedded val book: Book,
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
)

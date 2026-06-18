package net.yourein.rebro.interfaces

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Bookshelf

interface BookshelfRepository {
    fun getBookshelves(): Flow<List<Bookshelf>>

    suspend fun getBookshelf(bookshelfId: Long): Bookshelf?

    /** 指定 ID の書籍が属する本棚を取得する（書籍 → 本棚の逆引き）。 */
    suspend fun getBookshelfByBook(bookId: Long): Bookshelf?

    suspend fun addBookshelf(bookshelf: Bookshelf): Long

    suspend fun updateBookshelf(bookshelf: Bookshelf)

    suspend fun deleteBookshelf(bookshelf: Bookshelf)
}

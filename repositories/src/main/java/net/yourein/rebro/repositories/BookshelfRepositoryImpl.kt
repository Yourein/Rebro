package net.yourein.rebro.repositories

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.model.entity.Bookshelf

class BookshelfRepositoryImpl(
    private val bookshelfDao: BookshelfDao,
) : BookshelfRepository {

    override fun getBookshelves(): Flow<List<Bookshelf>> =
        bookshelfDao.getBookshelves()

    override suspend fun getBookshelf(bookshelfId: Long): Bookshelf? =
        bookshelfDao.getBookshelf(bookshelfId)

    override suspend fun addBookshelf(bookshelf: Bookshelf): Long =
        bookshelfDao.insertBookshelf(bookshelf)

    override suspend fun updateBookshelf(bookshelf: Bookshelf) =
        bookshelfDao.updateBookshelf(bookshelf)

    override suspend fun deleteBookshelf(bookshelf: Bookshelf) =
        bookshelfDao.deleteBookshelf(bookshelf)
}

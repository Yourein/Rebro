package net.yourein.rebro.repositories

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.BookAuthor
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.relation.BookWithDetailAndAuthors

class BookRepositoryImpl(
    private val bookDao: BookDao,
) : BookRepository {
    override fun getAllBooks(): Flow<List<BookWithDetailAndAuthors>> =
        bookDao.getAllBooks()

    override fun getBooksInBookshelf(bookshelfId: Long): Flow<List<Book>> =
        bookDao.getBooksInBookshelf(bookshelfId)

    override fun getBooksByAuthor(authorId: Long): Flow<List<Book>> =
        bookDao.getBooksByAuthor(authorId)

    override fun searchBooks(query: String): Flow<List<BookWithDetailAndAuthors>> =
        bookDao.searchBooks(query)

    override suspend fun getBook(bookId: Long): Book? =
        bookDao.getBook(bookId)

    override suspend fun getBookWithDetail(bookId: Long): BookWithDetailAndAuthors? =
        bookDao.getBookWithDetail(bookId)

    override fun getRecentRegisteredBooks(): Flow<List<BookWithDetailAndAuthors>> =
        bookDao.getRecentRegisteredBooks()

    override suspend fun addBook(book: Book): Long =
        bookDao.insertBook(book)

    override suspend fun addBookWithAuthors(book: Book, authorIds: List<Long>): Long =
        bookDao.insertBookWithAuthors(book, authorIds)

    override suspend fun updateBook(book: Book) =
        bookDao.updateBook(book)

    override suspend fun deleteBook(book: Book) =
        bookDao.deleteBook(book)

    // ── 商業本の詳細 ──────────────────────────────
    override suspend fun addCommercialDetail(detail: CommercialBookDetail): Long =
        bookDao.insertCommercialDetail(detail)

    override suspend fun updateCommercialDetail(detail: CommercialBookDetail) =
        bookDao.updateCommercialDetail(detail)

    override suspend fun deleteCommercialDetail(detail: CommercialBookDetail) =
        bookDao.deleteCommercialDetail(detail)

    // ── 同人本の詳細 ──────────────────────────────
    override suspend fun addDoujinDetail(detail: DoujinBookDetail): Long =
        bookDao.insertDoujinDetail(detail)

    override suspend fun updateDoujinDetail(detail: DoujinBookDetail) =
        bookDao.updateDoujinDetail(detail)

    override suspend fun deleteDoujinDetail(detail: DoujinBookDetail) =
        bookDao.deleteDoujinDetail(detail)

    // ── 本と著者の関連（結合テーブル）────────────────
    override suspend fun addAuthorToBook(bookId: Long, authorId: Long) =
        bookDao.insertBookAuthor(BookAuthor(bookId = bookId, authorId = authorId))

    override suspend fun removeAuthorFromBook(bookId: Long, authorId: Long) =
        bookDao.deleteBookAuthor(BookAuthor(bookId = bookId, authorId = authorId))
}

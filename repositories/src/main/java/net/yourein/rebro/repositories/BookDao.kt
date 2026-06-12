package net.yourein.rebro.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.BookAuthor
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.relation.BookWithAuthors
import net.yourein.rebro.model.relation.BookWithDetail

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE bookshelf_id = :bookshelfId ORDER BY id ASC")
    fun getBooksInBookshelf(bookshelfId: Long): Flow<List<Book>>

    @Transaction
    @Query(
        """
        SELECT b.* FROM books b
        INNER JOIN book_authors ba ON b.id = ba.book_id
        WHERE ba.author_id = :authorId
        ORDER BY b.id ASC
        """
    )
    fun getBooksByAuthor(authorId: Long): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: Long): Book?

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithAuthors(bookId: Long): BookWithAuthors?

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithDetail(bookId: Long): BookWithDetail?

    @Insert
    suspend fun insertBook(book: Book): Long

    /**
     * 本を挿入し、指定著者との関連を同一トランザクションで張る。
     * 途中で例外・キャンセルが発生した場合は全てロールバックされ、
     * 「著者リンクの無い本」が残ることはない。
     */
    @Transaction
    suspend fun insertBookWithAuthors(book: Book, authorIds: List<Long>): Long {
        val bookId = insertBook(book)
        authorIds.forEach { authorId ->
            insertBookAuthor(BookAuthor(bookId = bookId, authorId = authorId))
        }
        return bookId
    }

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    // ── 商業本の詳細 ──────────────────────────────
    @Insert
    suspend fun insertCommercialDetail(detail: CommercialBookDetail): Long

    @Update
    suspend fun updateCommercialDetail(detail: CommercialBookDetail)

    @Delete
    suspend fun deleteCommercialDetail(detail: CommercialBookDetail)

    // ── 同人本の詳細 ──────────────────────────────
    @Insert
    suspend fun insertDoujinDetail(detail: DoujinBookDetail): Long

    @Update
    suspend fun updateDoujinDetail(detail: DoujinBookDetail)

    @Delete
    suspend fun deleteDoujinDetail(detail: DoujinBookDetail)

    // ── 本と著者の関連（結合テーブル）────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookAuthor(bookAuthor: BookAuthor)

    @Delete
    suspend fun deleteBookAuthor(bookAuthor: BookAuthor)
}

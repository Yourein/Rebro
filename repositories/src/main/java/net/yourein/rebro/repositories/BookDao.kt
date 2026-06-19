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
import net.yourein.rebro.model.entity.BookSeries
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.relation.BookWithDetailAndAuthors

@Dao
interface BookDao {
    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<BookWithDetailAndAuthors>>

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

    /**
     * 書名・サブタイトル・ISBN・ISDN を横断して曖昧検索する。
     * 入力がタイトルか管理番号かを呼び出し側で判別する必要はなく、
     * 4カラムへの中間一致 OR 検索で一致した本を返す。
     * 商業本／同人本で詳細テーブルの有無が分かれるため LEFT JOIN を用いる。
     */
    @Transaction
    @Query(
        """
        SELECT DISTINCT b.* FROM books b
        LEFT JOIN commercial_book_details c ON b.id = c.book_id
        LEFT JOIN doujin_book_details d ON b.id = d.book_id
        WHERE b.title    LIKE '%' || :query || '%'
           OR b.subtitle LIKE '%' || :query || '%'
           OR c.isbn     LIKE '%' || :query || '%'
           OR d.isdn     LIKE '%' || :query || '%'
        ORDER BY b.id ASC
        """
    )
    fun searchBooks(query: String): Flow<List<BookWithDetailAndAuthors>>

    /**
     * **登録** が新しい順に15件を取得する。
     * SQLiteの自動生成idに依存していることに注意。
     */
    @Transaction
    @Query(
        """
        SELECT b.* FROM books b
        ORDER BY b.id DESC
        LIMIT 15
        """
    )
    fun getRecentRegisteredBooks(): Flow<List<BookWithDetailAndAuthors>>

    @Transaction
    @Query(
        """
        SELECT b.* FROM books b
        INNER JOIN doujin_book_details d ON b.id = d.book_id
        WHERE d.circle_id = :circleId
        ORDER BY b.id ASC
        """
    )
    fun getBooksByCircle(circleId: Long): Flow<List<BookWithDetailAndAuthors>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: Long): Book?

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookWithDetail(bookId: Long): BookWithDetailAndAuthors?

    @Insert
    suspend fun insertBook(book: Book): Long

    /**
     * 本を挿入し、指定著者との関連を同一トランザクションで張る。
     * 途中で例外・キャンセルが発生した場合は全てロールバックされ、
     * 「著者リンクの無い本」が残ることはない。
     */
    @Transaction
    suspend fun insertBookWithAuthors(
        book: Book,
        authorIds: List<Long>,
        seriesIds: List<Long> = emptyList(),
    ): Long {
        val bookId = insertBook(book)
        authorIds.forEach { authorId ->
            insertBookAuthor(BookAuthor(bookId = bookId, authorId = authorId))
        }
        seriesIds.forEach { seriesId ->
            insertBookSeries(BookSeries(bookId = bookId, seriesId = seriesId))
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

    // ── 本とシリーズの関連（結合テーブル）────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookSeries(bookSeries: BookSeries)

    @Delete
    suspend fun deleteBookSeries(bookSeries: BookSeries)

    @Transaction
    @Query(
        """
        SELECT b.* FROM books b
        INNER JOIN book_series bs ON b.id = bs.book_id
        WHERE bs.series_id = :seriesId
        ORDER BY b.id ASC
        """
    )
    fun getBooksBySeries(seriesId: Long): Flow<List<BookWithDetailAndAuthors>>
}

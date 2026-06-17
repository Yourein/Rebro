package net.yourein.rebro.interfaces

import kotlinx.coroutines.flow.Flow
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.relation.BookWithAuthors
import net.yourein.rebro.model.relation.BookWithDetail

interface BookRepository {
    fun getAllBooks(): Flow<List<BookWithDetail>>

    fun getBooksInBookshelf(bookshelfId: Long): Flow<List<Book>>

    /** 指定著者が書いた本を取得する（著者での絞り込み）。 */
    fun getBooksByAuthor(authorId: Long): Flow<List<Book>>

    /**
     * 書名・ISBN・ISDN を横断して曖昧検索する。
     * 入力がタイトルか管理番号かを呼び出し側で判別する必要はない。
     */
    fun searchBooks(query: String): Flow<List<BookWithDetail>>

    suspend fun getBook(bookId: Long): Book?

    suspend fun getBookWithAuthors(bookId: Long): BookWithAuthors?

    suspend fun getBookWithDetail(bookId: Long): BookWithDetail?

    suspend fun addBook(book: Book): Long

    /**
     * 本を追加し、指定著者との関連を1トランザクションで張る。
     * 例外・コルーチンのキャンセルが起きても全てロールバックされるため、
     * 「著者リンクの無い本」が中途半端に残らない。
     *
     * @param authorIds 事前に解決済み（find-or-create 済み）の著者ID
     * @return 追加された本のID
     */
    suspend fun addBookWithAuthors(book: Book, authorIds: List<Long>): Long

    suspend fun updateBook(book: Book)

    suspend fun deleteBook(book: Book)

    // ── 商業本の詳細 ──────────────────────────────
    suspend fun addCommercialDetail(detail: CommercialBookDetail): Long

    suspend fun updateCommercialDetail(detail: CommercialBookDetail)

    suspend fun deleteCommercialDetail(detail: CommercialBookDetail)

    // ── 同人本の詳細 ──────────────────────────────
    suspend fun addDoujinDetail(detail: DoujinBookDetail): Long

    suspend fun updateDoujinDetail(detail: DoujinBookDetail)

    suspend fun deleteDoujinDetail(detail: DoujinBookDetail)

    // ── 本と著者の関連（結合テーブル）────────────────
    suspend fun addAuthorToBook(bookId: Long, authorId: Long)

    suspend fun removeAuthorFromBook(bookId: Long, authorId: Long)
}

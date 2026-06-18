package net.yourein.rebro.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.model.uimodel.BookUiModel
import net.yourein.rebro.model.uimodel.toUiModel

/**
 * 全書籍を画面表示用の [BookUiModel] のリストとして購読する。
 *
 * Repository が返す Room 関連モデルを UI モデルへ変換する責務を担い、
 * ViewModel からは表示に必要な形だけが見えるようにする。
 */
class BooksUseCase(
    private val bookRepository: BookRepository,
) {
    fun getAllBooks(): Flow<List<BookUiModel>> =
        bookRepository.getAllBooks()
            .map { books -> books.map { it.toUiModel() } }

    fun getRecentRegisteredBooks(): Flow<List<BookUiModel>> =
        bookRepository.getRecentRegisteredBooks()
            .map { books -> books.map { it.toUiModel() } }

    /**
     * 書名・ISBN・ISDN を横断した曖昧検索の結果を [BookUiModel] のリストとして購読する。
     * 入力がタイトルか管理番号かの判別は Repository 側に委ねる。
     */
    fun searchBooks(query: String): Flow<List<BookUiModel>> =
        bookRepository.searchBooks(query)
            .map { books -> books.map { it.toUiModel() } }

    /**
     * 指定 ID の書籍を、詳細情報・著者まで含めた [BookUiModel] として取得する。
     * 該当する書籍が存在しない場合は null を返す。
     */
    suspend fun getBook(bookId: Long): BookUiModel? =
        bookRepository.getBookWithDetail(bookId)?.toUiModel()
}

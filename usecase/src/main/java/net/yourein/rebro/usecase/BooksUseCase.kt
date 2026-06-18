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
    operator fun invoke(): Flow<List<BookUiModel>> =
        bookRepository.getAllBooks()
            .map { books -> books.map { it.toUiModel() } }
}

package net.yourein.rebro.feature.bookdetail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.uimodel.BookUiModel
import net.yourein.rebro.usecase.BooksUseCase
import net.yourein.rebro.usecase.BookshelfUseCase

/**
 * 書籍詳細画面の状態を保持する ViewModel。
 *
 * 表示対象は [bookId] で固定され、生成時に一度だけ取得する。
 * 取得結果は [LoadingState] でラップし、読み込み中・成功・失敗（該当書籍なしを含む）を表現する。
 *
 * 書籍が属する本棚は [BookUiModel] には持たせず、[bookId] からの逆引きで別途取得する
 * （本棚へのリンク表示にのみ使う補助情報のため）。
 *
 * @param bookId 表示する書籍の ID。生成時に外部から渡す。
 */
class BookDetailViewModel(
    private val bookId: Long,
    private val booksUseCase: BooksUseCase,
    private val bookshelfUseCase: BookshelfUseCase,
) : ViewModel() {
    init {
        viewModelScope.launch {
            bookState = try {
                val book = booksUseCase.getBook(bookId)
                if (book == null) {
                    LoadingState.Error(null, Throwable("Book not found."))
                } else {
                    LoadingState.Success(book)
                }
            } catch (e: Throwable) {
                LoadingState.Error(null, e)
            }

            bookshelf = try {
                bookshelfUseCase.getBookshelfByBook(bookId)
            } catch (e: Throwable) {
                Log.e("BookDetailViewModel", "$e")
                null
            }
        }
    }

    var bookState: LoadingState<BookUiModel> by mutableStateOf(LoadingState.Loading(null))
        private set

    var bookshelf: Bookshelf? by mutableStateOf(null)
        private set
}

package net.yourein.rebro.feature.searchtop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.model.uimodel.BookUiModel
import net.yourein.rebro.usecase.BooksUseCase

class SearchTopViewModel(
    private val booksUseCase: BooksUseCase,
): ViewModel() {
    val recentBooksState: StateFlow<LoadingState<List<BookUiModel>>> = booksUseCase
        .getRecentRegisteredBooks()
        .map<List<BookUiModel>, LoadingState<List<BookUiModel>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}
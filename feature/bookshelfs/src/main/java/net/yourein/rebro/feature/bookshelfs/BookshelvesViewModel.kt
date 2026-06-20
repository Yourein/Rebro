package net.yourein.rebro.feature.bookshelfs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.model.entity.Bookshelf

class BookshelvesViewModel(
    bookshelfRepository: BookshelfRepository,
) : ViewModel() {
    val bookshelvesState: StateFlow<LoadingState<List<Bookshelf>>> = bookshelfRepository
        .getBookshelves()
        .map<List<Bookshelf>, LoadingState<List<Bookshelf>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}

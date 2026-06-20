package net.yourein.rebro.feature.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.model.relation.BookWithDetailAndAuthors

class BooksViewModel(
    bookRepository: BookRepository,
) : ViewModel() {
    val booksState: StateFlow<LoadingState<List<BookWithDetailAndAuthors>>> = bookRepository
        .getAllBooks()
        .map<List<BookWithDetailAndAuthors>, LoadingState<List<BookWithDetailAndAuthors>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}

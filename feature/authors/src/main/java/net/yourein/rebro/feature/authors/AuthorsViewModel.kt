package net.yourein.rebro.feature.authors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.model.entity.Author

class AuthorsViewModel(
    authorRepository: AuthorRepository,
) : ViewModel() {
    val authorsState: StateFlow<LoadingState<List<Author>>> = authorRepository
        .getAuthors()
        .map<List<Author>, LoadingState<List<Author>>> {
            LoadingState.Success(it)
        }
        .catch { e -> emit(LoadingState.Error(null, e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Loading(null),
        )
}

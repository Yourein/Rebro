package net.yourein.rebro.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.model.uimodel.BookUiModel
import net.yourein.rebro.usecase.BooksUseCase

/**
 * 検索画面の状態を保持する ViewModel。
 *
 * 入力中の検索クエリ（[query]）を購読し、[debounce] で打鍵ごとの過剰な検索を抑えつつ、
 * [flatMapLatest] で最新クエリの検索結果だけを反映する（古いクエリの結果は破棄される）。
 * クエリが空のときは検索を走らせず、結果を空リストとして扱う。
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val booksUseCase: BooksUseCase,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResultState: StateFlow<LoadingState<List<BookUiModel>>> = _query
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { trimmed ->
            if (trimmed.isEmpty()) {
                flowOf(LoadingState.Success(emptyList()))
            } else {
                booksUseCase.searchBooks(trimmed)
                    .map<List<BookUiModel>, LoadingState<List<BookUiModel>>> {
                        LoadingState.Success(it)
                    }
                    .onStart { emit(LoadingState.Loading(null)) }
                    .catch { e -> emit(LoadingState.Error(null, e)) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoadingState.Success(emptyList()),
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }
}

/** 打鍵から検索開始までの待機時間（ミリ秒）。入力が落ち着いてから検索する。 */
private const val SEARCH_DEBOUNCE_MILLIS = 300L

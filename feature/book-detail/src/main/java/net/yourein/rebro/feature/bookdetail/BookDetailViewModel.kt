package net.yourein.rebro.feature.bookdetail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.interfaces.SeriesRepository
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.ReadingStatus
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.Series
import net.yourein.rebro.model.relation.BookWithDetailAndAuthors
import net.yourein.rebro.model.uimodel.BookUiModel
import net.yourein.rebro.model.uimodel.toUiModel
import net.yourein.rebro.usecase.BooksUseCase
import net.yourein.rebro.usecase.BookshelfUseCase

class BookDetailViewModel(
    private val bookId: Long,
    private val booksUseCase: BooksUseCase,
    private val bookshelfUseCase: BookshelfUseCase,
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val circleRepository: CircleRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {
    init {
        viewModelScope.launch {
            bookState = try {
                rawBook = bookRepository.getBookWithDetail(bookId)
                val book = rawBook?.toUiModel()
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

    private var rawBook: BookWithDetailAndAuthors? = null

    // ── 編集モード ──────────────────────────────────

    var isEditing: Boolean by mutableStateOf(false)
        private set

    var editTitle by mutableStateOf("")
        private set
    var editSubtitle by mutableStateOf("")
        private set
    var editReadingStatus by mutableStateOf(ReadingStatus.UNREAD)
        private set
    var editPublisher by mutableStateOf("")
        private set
    var editIsbn by mutableStateOf("")
        private set
    var editIsdn by mutableStateOf("")
        private set

    fun updateEditTitle(value: String) { editTitle = value }
    fun updateEditSubtitle(value: String) { editSubtitle = value }
    fun updateEditReadingStatus(value: ReadingStatus) { editReadingStatus = value }
    fun updateEditPublisher(value: String) { editPublisher = value }
    fun updateEditIsbn(value: String) { editIsbn = value }
    fun updateEditIsdn(value: String) { editIsdn = value }

    // ── 著者選択 ──────────────────────────────────

    val allAuthors: StateFlow<List<Author>> = authorRepository.getAuthors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editSelectedAuthors = MutableStateFlow<List<Author>>(emptyList())
    val editSelectedAuthors: StateFlow<List<Author>> = _editSelectedAuthors.asStateFlow()

    fun toggleAuthor(author: Author) {
        val current = _editSelectedAuthors.value
        _editSelectedAuthors.value = if (current.any { it.id == author.id }) {
            current.filter { it.id != author.id }
        } else {
            current + author
        }
    }

    fun removeAuthor(author: Author) {
        _editSelectedAuthors.value = _editSelectedAuthors.value.filter { it.id != author.id }
    }

    fun addNewAuthor(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                authorRepository.findAuthorByName(trimmed)
                    ?: Author(
                        id = authorRepository.addAuthor(Author(name = trimmed)),
                        name = trimmed,
                    )
            }.onSuccess { author ->
                if (_editSelectedAuthors.value.none { it.id == author.id }) {
                    _editSelectedAuthors.value = _editSelectedAuthors.value + author
                }
            }
        }
    }

    fun renameAuthor(authorId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                val renamed = Author(id = authorId, name = trimmed)
                authorRepository.updateAuthor(renamed)
                renamed
            }.onSuccess { renamed ->
                _editSelectedAuthors.value = _editSelectedAuthors.value.map {
                    if (it.id == renamed.id) renamed else it
                }
            }
        }
    }

    // ── 本棚選択 ──────────────────────────────────

    val allBookshelves: StateFlow<List<Bookshelf>> = bookshelfRepository.getBookshelves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editSelectedBookshelf = MutableStateFlow<Bookshelf?>(null)
    val editSelectedBookshelf: StateFlow<Bookshelf?> = _editSelectedBookshelf.asStateFlow()

    fun setEditBookshelf(bookshelf: Bookshelf?) {
        _editSelectedBookshelf.value = bookshelf
    }

    fun addNewBookshelf(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                bookshelfRepository.findBookshelfByName(trimmed)
                    ?: Bookshelf(
                        id = bookshelfRepository.addBookshelf(Bookshelf(name = trimmed)),
                        name = trimmed,
                    )
            }.onSuccess { bookshelf ->
                _editSelectedBookshelf.value = bookshelf
            }
        }
    }

    // ── サークル選択 ─────────────────────────────

    val allCircles: StateFlow<List<Circle>> = circleRepository.getCircles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editSelectedCircle = MutableStateFlow<Circle?>(null)
    val editSelectedCircle: StateFlow<Circle?> = _editSelectedCircle.asStateFlow()

    fun setEditCircle(circle: Circle?) {
        _editSelectedCircle.value = circle
    }

    fun addNewCircle(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                circleRepository.findCircleByName(trimmed)
                    ?: Circle(
                        id = circleRepository.addCircle(Circle(name = trimmed)),
                        name = trimmed,
                    )
            }.onSuccess { circle ->
                _editSelectedCircle.value = circle
            }
        }
    }

    fun renameCircle(circleId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                val renamed = Circle(id = circleId, name = trimmed)
                circleRepository.updateCircle(renamed)
                renamed
            }.onSuccess { renamed ->
                if (_editSelectedCircle.value?.id == renamed.id) {
                    _editSelectedCircle.value = renamed
                }
            }
        }
    }

    // ── シリーズ選択 ──────────────────────────────

    val allSeries: StateFlow<List<Series>> = seriesRepository.getSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editSelectedSeries = MutableStateFlow<List<Series>>(emptyList())
    val editSelectedSeries: StateFlow<List<Series>> = _editSelectedSeries.asStateFlow()

    fun toggleSeries(series: Series) {
        val current = _editSelectedSeries.value
        _editSelectedSeries.value = if (current.any { it.id == series.id }) {
            current.filter { it.id != series.id }
        } else {
            current + series
        }
    }

    fun removeSeries(series: Series) {
        _editSelectedSeries.value = _editSelectedSeries.value.filter { it.id != series.id }
    }

    fun addNewSeries(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                seriesRepository.findSeriesByName(trimmed)
                    ?: Series(
                        id = seriesRepository.addSeries(Series(name = trimmed)),
                        name = trimmed,
                    )
            }.onSuccess { series ->
                if (_editSelectedSeries.value.none { it.id == series.id }) {
                    _editSelectedSeries.value = _editSelectedSeries.value + series
                }
            }
        }
    }

    // ── 編集開始/キャンセル/保存 ────────────────────

    fun startEditing() {
        val raw = rawBook ?: return
        val uiModel = (bookState as? LoadingState.Success)?.value ?: return

        editTitle = uiModel.title
        editSubtitle = uiModel.subtitle.orEmpty()
        editReadingStatus = uiModel.readingStatus
        _editSelectedAuthors.value = raw.authors
        _editSelectedBookshelf.value = bookshelf
        _editSelectedSeries.value = raw.series

        when (uiModel) {
            is BookUiModel.Commercial -> {
                editPublisher = uiModel.publisher.orEmpty()
                editIsbn = uiModel.isbn.orEmpty()
            }
            is BookUiModel.Doujin -> {
                val circle = raw.doujinDetail?.circle
                _editSelectedCircle.value = circle
                editIsdn = uiModel.isdn.orEmpty()
            }
        }

        isEditing = true
    }

    fun cancelEditing() {
        isEditing = false
    }

    fun saveChanges() {
        val raw = rawBook ?: return
        val selectedBookshelf = _editSelectedBookshelf.value
        val trimmedTitle = editTitle.trim()
        if (trimmedTitle.isEmpty() || selectedBookshelf == null) return

        viewModelScope.launch {
            runCatching {
                val updatedBook = raw.book.copy(
                    title = trimmedTitle,
                    subtitle = editSubtitle.trim().ifEmpty { null },
                    readingStatus = editReadingStatus,
                    bookshelfId = selectedBookshelf.id,
                )
                bookRepository.updateBook(updatedBook)

                // 著者の差分更新
                val oldAuthorIds = raw.authors.map { it.id }.toSet()
                val newAuthorIds = _editSelectedAuthors.value.map { it.id }.toSet()
                (newAuthorIds - oldAuthorIds).forEach { id ->
                    bookRepository.addAuthorToBook(bookId, id)
                }
                (oldAuthorIds - newAuthorIds).forEach { id ->
                    bookRepository.removeAuthorFromBook(bookId, id)
                }

                // シリーズの差分更新
                val oldSeriesIds = raw.series.map { it.id }.toSet()
                val newSeriesIds = _editSelectedSeries.value.map { it.id }.toSet()
                (newSeriesIds - oldSeriesIds).forEach { id ->
                    bookRepository.addSeriesToBook(bookId, id)
                }
                (oldSeriesIds - newSeriesIds).forEach { id ->
                    bookRepository.removeSeriesFromBook(bookId, id)
                }

                // 詳細テーブルの更新
                when (raw.book.bookType) {
                    BookType.COMMERCIAL -> {
                        val detail = raw.commercialDetail
                        if (detail != null) {
                            bookRepository.updateCommercialDetail(
                                detail.copy(
                                    publisher = editPublisher.trim().ifEmpty { null },
                                    isbn = editIsbn.trim().ifEmpty { null },
                                )
                            )
                        }
                    }
                    BookType.DOUJIN -> {
                        val detail = raw.doujinDetail?.detail
                        if (detail != null) {
                            bookRepository.updateDoujinDetail(
                                detail.copy(
                                    circleId = _editSelectedCircle.value?.id,
                                    isdn = editIsdn.trim().ifEmpty { null },
                                )
                            )
                        }
                    }
                }
            }.onSuccess {
                val updated = bookRepository.getBookWithDetail(bookId)
                rawBook = updated
                bookState = if (updated != null) {
                    LoadingState.Success(updated.toUiModel())
                } else {
                    LoadingState.Error(null, Throwable("Book not found."))
                }
                bookshelf = selectedBookshelf
                isEditing = false
            }
        }
    }
}

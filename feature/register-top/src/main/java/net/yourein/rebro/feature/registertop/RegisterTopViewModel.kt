package net.yourein.rebro.feature.registertop

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.interfaces.SeriesRepository
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import net.yourein.rebro.model.entity.Series
import java.io.File
import java.net.URL
import java.util.UUID

data class AutofillResult(
    val title: String,
    val bookType: BookType,
    val publisher: String,
    val authorNames: List<String>,
    val circleName: String?,
    val coverImageUrl: String?,
)

/** 書籍登録画面の ViewModel。 */
class RegisterTopViewModel(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val circleRepository: CircleRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    fun consumeRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    private val _coverImagePath = MutableStateFlow<String?>(null)
    val coverImagePath: StateFlow<String?> = _coverImagePath.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    // ── 本棚選択 ──────────────────────────────────

    val allBookshelves: StateFlow<List<Bookshelf>> = bookshelfRepository.getBookshelves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedBookshelf = MutableStateFlow<Bookshelf?>(null)
    val selectedBookshelf: StateFlow<Bookshelf?> = _selectedBookshelf.asStateFlow()

    fun setBookshelf(bookshelf: Bookshelf?) {
        _selectedBookshelf.value = bookshelf
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
                _selectedBookshelf.value = bookshelf
            }.onFailure { e ->
                _lastResult.value = "本棚の追加に失敗しました：${e.message}"
            }
        }
    }

    // ── 著者選択 ──────────────────────────────────

    val allAuthors: StateFlow<List<Author>> = authorRepository.getAuthors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedAuthors = MutableStateFlow<List<Author>>(emptyList())
    val selectedAuthors: StateFlow<List<Author>> = _selectedAuthors.asStateFlow()

    fun toggleAuthor(author: Author) {
        val current = _selectedAuthors.value
        _selectedAuthors.value = if (current.any { it.id == author.id }) {
            current.filter { it.id != author.id }
        } else {
            current + author
        }
    }

    fun removeAuthor(author: Author) {
        _selectedAuthors.value = _selectedAuthors.value.filter { it.id != author.id }
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
                if (_selectedAuthors.value.none { it.id == author.id }) {
                    _selectedAuthors.value = _selectedAuthors.value + author
                }
            }.onFailure { e ->
                _lastResult.value = "著者の追加に失敗しました：${e.message}"
            }
        }
    }

    // ── サークル選択 ─────────────────────────────

    val allCircles: StateFlow<List<Circle>> = circleRepository.getCircles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedCircle = MutableStateFlow<Circle?>(null)
    val selectedCircle: StateFlow<Circle?> = _selectedCircle.asStateFlow()

    fun setCircle(circle: Circle?) {
        _selectedCircle.value = circle
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
                _selectedCircle.value = circle
            }.onFailure { e ->
                _lastResult.value = "サークルの追加に失敗しました：${e.message}"
            }
        }
    }

    // ── シリーズ選択 ──────────────────────────────

    val allSeries: StateFlow<List<Series>> = seriesRepository.getSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedSeries = MutableStateFlow<List<Series>>(emptyList())
    val selectedSeries: StateFlow<List<Series>> = _selectedSeries.asStateFlow()

    fun toggleSeries(series: Series) {
        val current = _selectedSeries.value
        _selectedSeries.value = if (current.any { it.id == series.id }) {
            current.filter { it.id != series.id }
        } else {
            current + series
        }
    }

    fun removeSeries(series: Series) {
        _selectedSeries.value = _selectedSeries.value.filter { it.id != series.id }
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
                if (_selectedSeries.value.none { it.id == series.id }) {
                    _selectedSeries.value = _selectedSeries.value + series
                }
            }.onFailure { e ->
                _lastResult.value = "シリーズの追加に失敗しました：${e.message}"
            }
        }
    }

    // ── カバー画像 ───────────────────────────────

    fun saveCoverImageFromPicker(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            deletePreviousCoverFile()
            runCatching {
                val file = createCoverFile()
                application.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalStateException("Cannot open input stream")
                file.absolutePath
            }.onSuccess { path ->
                _coverImagePath.value = path
            }.onFailure { e ->
                _lastResult.value = "画像の保存に失敗しました：${e.message}"
            }
        }
    }

    fun downloadCoverImage(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isDownloading.value = true
            deletePreviousCoverFile()
            runCatching {
                val file = createCoverFile()
                URL(url).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            }.onSuccess { path ->
                _coverImagePath.value = path
            }.onFailure { e ->
                _lastResult.value = "画像のダウンロードに失敗しました：${e.message}"
            }
            _isDownloading.value = false
        }
    }

    fun clearCoverImage() {
        val path = _coverImagePath.value ?: return
        _coverImagePath.value = null
        viewModelScope.launch(Dispatchers.IO) {
            File(path).delete()
        }
    }

    // ── Autofill ─────────────────────────────────

    fun applyAutofill(result: AutofillResult) {
        _selectedAuthors.value = emptyList()
        _selectedCircle.value = null
        _selectedSeries.value = emptyList()
        _selectedBookshelf.value = null
        clearCoverImage()

        result.authorNames.forEach { addNewAuthor(it) }
        result.circleName?.let { addNewCircle(it) }
        result.coverImageUrl?.let { downloadCoverImage(it) }
    }

    // ── 登録 ─────────────────────────────────────

    fun registerBook(
        title: String,
        subtitle: String,
        bookType: BookType,
        publisher: String,
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            _lastResult.value = "タイトルを入力してください"
            return
        }
        val bookshelfId = _selectedBookshelf.value?.id
        if (bookshelfId == null) {
            _lastResult.value = "本棚を選択してください"
            return
        }
        viewModelScope.launch {
            runCatching {
                val authorIds = _selectedAuthors.value.map { it.id }
                val seriesIds = _selectedSeries.value.map { it.id }
                val bookId = bookRepository.addBookWithAuthors(
                    book = Book(
                        bookshelfId = bookshelfId,
                        title = trimmedTitle,
                        subtitle = subtitle.trim().ifEmpty { null },
                        bookType = bookType,
                        thumbnailPath = _coverImagePath.value,
                    ),
                    authorIds = authorIds,
                    seriesIds = seriesIds,
                )
                when (bookType) {
                    BookType.COMMERCIAL -> bookRepository.addCommercialDetail(
                        CommercialBookDetail(
                            bookId = bookId,
                            publisher = publisher.trim().ifEmpty { null },
                        )
                    )

                    BookType.DOUJIN -> bookRepository.addDoujinDetail(
                        DoujinBookDetail(
                            bookId = bookId,
                            circleId = _selectedCircle.value?.id,
                        )
                    )
                }
                bookId
            }.onSuccess { bookId ->
                _lastResult.value = "登録しました（bookId=$bookId）：$trimmedTitle"
                _coverImagePath.value = null
                _selectedBookshelf.value = null
                _selectedAuthors.value = emptyList()
                _selectedSeries.value = emptyList()
                _selectedCircle.value = null
                _registrationSuccess.value = true
            }.onFailure { e ->
                _lastResult.value = "登録に失敗しました：${e.message}"
            }
        }
    }

    // ── private helpers ──────────────────────────

    private fun deletePreviousCoverFile() {
        _coverImagePath.value?.let { File(it).delete() }
    }

    private fun createCoverFile(): File {
        val dir = File(application.filesDir, COVER_IMAGES_DIR)
        dir.mkdirs()
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    private companion object {
        const val COVER_IMAGES_DIR = "cover_images"
    }
}

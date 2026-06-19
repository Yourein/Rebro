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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.yourein.rebro.interfaces.AuthorRepository
import net.yourein.rebro.interfaces.BookRepository
import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.interfaces.CircleRepository
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail
import java.io.File
import java.net.URL
import java.util.UUID

/**
 * 【デバッグ用】登録トップ画面の ViewModel。
 *
 * 検索トップの「最近登録した本（15件）」表示を動作確認するために、
 * 任意の本を手早く DB へ登録できるようにする臨時実装。
 * register-top 画面が本来の仕様で作り直される際に丸ごと差し替える前提のため、
 * 専用 UseCase は設けず Repository を直接叩いている。
 */
class RegisterTopViewModel(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val circleRepository: CircleRepository,
) : ViewModel() {

    /** 現在 DB に登録されている本の総数（デバッグ表示用）。 */
    private val bookCount: StateFlow<Int> = bookRepository.getAllBooks()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private val _coverImagePath = MutableStateFlow<String?>(null)
    val coverImagePath: StateFlow<String?> = _coverImagePath.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

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
        viewModelScope.launch {
            runCatching {
                val bookshelfId = ensureDebugBookshelf()
                val authorIds = _selectedAuthors.value.map { it.id }
                val bookId = bookRepository.addBookWithAuthors(
                    book = Book(
                        bookshelfId = bookshelfId,
                        title = trimmedTitle,
                        subtitle = subtitle.trim().ifEmpty { null },
                        bookType = bookType,
                        thumbnailPath = _coverImagePath.value,
                    ),
                    authorIds = authorIds,
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
                _selectedAuthors.value = emptyList()
                _selectedCircle.value = null
            }.onFailure { e ->
                _lastResult.value = "登録に失敗しました：${e.message}"
            }
        }
    }

    fun registerRandomBook() {
        val n = bookCount.value + 1
        val isCommercial = n % 2 == 0
        viewModelScope.launch {
            val authorName = "テスト著者$n"
            val author = authorRepository.findAuthorByName(authorName)
                ?: Author(
                    id = authorRepository.addAuthor(Author(name = authorName)),
                    name = authorName,
                )
            _selectedAuthors.value = listOf(author)

            if (!isCommercial) {
                val circleName = "サンプルサークル"
                val circle = circleRepository.findCircleByName(circleName)
                    ?: Circle(
                        id = circleRepository.addCircle(Circle(name = circleName)),
                        name = circleName,
                    )
                _selectedCircle.value = circle
            }

            registerBook(
                title = "サンプル本 #$n",
                subtitle = "",
                bookType = if (isCommercial) BookType.COMMERCIAL else BookType.DOUJIN,
                publisher = if (isCommercial) "サンプル出版社" else "",
            )
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

    private suspend fun ensureDebugBookshelf(): Long {
        val existing = bookshelfRepository.getBookshelves().first()
        val target = existing.firstOrNull { it.name == DEBUG_BOOKSHELF_NAME }
            ?: existing.firstOrNull()
        return target?.id
            ?: bookshelfRepository.addBookshelf(Bookshelf(name = DEBUG_BOOKSHELF_NAME))
    }

    private companion object {
        const val DEBUG_BOOKSHELF_NAME = "デバッグ本棚"
        const val COVER_IMAGES_DIR = "cover_images"
    }
}

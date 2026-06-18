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

    /** 直近の登録操作の結果メッセージ（成功・失敗どちらも）。 */
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private val _coverImagePath = MutableStateFlow<String?>(null)
    val coverImagePath: StateFlow<String?> = _coverImagePath.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

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

    /**
     * 入力内容から本を1冊登録する。
     *
     * 本は本棚への外部キーを持つため、デバッグ用本棚を find-or-create してから登録する。
     * 著者は「、」または「,」区切りで複数指定でき、既存名は再利用する。
     *
     * @param detail 商業誌なら出版社、同人誌ならサークル名として保存する。
     */
    fun registerBook(
        title: String,
        subtitle: String,
        authorNames: String,
        bookType: BookType,
        detail: String,
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            _lastResult.value = "タイトルを入力してください"
            return
        }
        viewModelScope.launch {
            runCatching {
                val bookshelfId = ensureDebugBookshelf()
                val authorIds = resolveAuthors(authorNames)
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
                            publisher = detail.trim().ifEmpty { null },
                        )
                    )

                    BookType.DOUJIN -> {
                        val circleId = resolveCircle(detail)
                        bookRepository.addDoujinDetail(
                            DoujinBookDetail(
                                bookId = bookId,
                                circleId = circleId,
                            )
                        )
                    }
                }
                bookId
            }.onSuccess { bookId ->
                _lastResult.value = "登録しました（bookId=$bookId）：$trimmedTitle"
                _coverImagePath.value = null
            }.onFailure { e ->
                _lastResult.value = "登録に失敗しました：${e.message}"
            }
        }
    }

    /** ワンタップでダミー本を登録する。連打して「最近登録した本」の挙動を確認する用途。 */
    fun registerRandomBook() {
        val n = bookCount.value + 1
        val isCommercial = n % 2 == 0
        registerBook(
            title = "サンプル本 #$n",
            subtitle = "",
            authorNames = "テスト著者$n",
            bookType = if (isCommercial) BookType.COMMERCIAL else BookType.DOUJIN,
            detail = if (isCommercial) "サンプル出版社" else "サンプルサークル",
        )
    }

    private fun deletePreviousCoverFile() {
        _coverImagePath.value?.let { File(it).delete() }
    }

    private fun createCoverFile(): File {
        val dir = File(application.filesDir, COVER_IMAGES_DIR)
        dir.mkdirs()
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    /**
     * 本登録に必要な本棚を確保する。
     * デバッグ用本棚があれば流用、無ければ既存の任意の本棚、それも無ければ新規作成する。
     */
    private suspend fun ensureDebugBookshelf(): Long {
        val existing = bookshelfRepository.getBookshelves().first()
        val target = existing.firstOrNull { it.name == DEBUG_BOOKSHELF_NAME }
            ?: existing.firstOrNull()
        return target?.id
            ?: bookshelfRepository.addBookshelf(Bookshelf(name = DEBUG_BOOKSHELF_NAME))
    }

    /** サークル名を find-or-create して ID に解決する。空文字なら null。 */
    private suspend fun resolveCircle(raw: String): Long? {
        val name = raw.trim()
        if (name.isEmpty()) return null
        return circleRepository.findCircleByName(name)?.id
            ?: circleRepository.addCircle(Circle(name = name))
    }

    /** 区切り文字で分割した著者名を find-or-create して ID リストに解決する。 */
    private suspend fun resolveAuthors(raw: String): List<Long> =
        raw.split(",", "、")
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { name ->
                authorRepository.findAuthorByName(name)?.id
                    ?: authorRepository.addAuthor(Author(name = name))
            }

    private companion object {
        const val DEBUG_BOOKSHELF_NAME = "デバッグ本棚"
        const val COVER_IMAGES_DIR = "cover_images"
    }
}

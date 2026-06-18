package net.yourein.rebro.feature.registertop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Book
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.CommercialBookDetail
import net.yourein.rebro.model.entity.DoujinBookDetail

/**
 * 【デバッグ用】登録トップ画面の ViewModel。
 *
 * 検索トップの「最近登録した本（15件）」表示を動作確認するために、
 * 任意の本を手早く DB へ登録できるようにする臨時実装。
 * register-top 画面が本来の仕様で作り直される際に丸ごと差し替える前提のため、
 * 専用 UseCase は設けず Repository を直接叩いている。
 */
class RegisterTopViewModel(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookshelfRepository: BookshelfRepository,
) : ViewModel() {

    /** 現在 DB に登録されている本の総数（デバッグ表示用）。 */
    val bookCount: StateFlow<Int> = bookRepository.getAllBooks()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    private val _lastResult = MutableStateFlow<String?>(null)

    /** 直近の登録操作の結果メッセージ（成功・失敗どちらも）。 */
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

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

                    BookType.DOUJIN -> bookRepository.addDoujinDetail(
                        DoujinBookDetail(
                            bookId = bookId,
                            circleName = detail.trim().ifEmpty { null },
                        )
                    )
                }
                bookId
            }.onSuccess { bookId ->
                _lastResult.value = "登録しました（bookId=$bookId）：$trimmedTitle"
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
    }
}

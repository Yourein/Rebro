package net.yourein.rebro.model.uimodel

import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.ReadingStatus
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Series
import net.yourein.rebro.model.relation.BookWithDetailAndAuthors

/**
 * 画面表示用の書籍モデル。
 *
 * 商業誌／同人誌で異なる詳細情報を nullable な単一型で持つのではなく、
 * [Commercial] / [Doujin] のサブクラスに分けて型で表現する。
 * これにより UI 側は `when` で網羅的に分岐でき、
 * 「商業誌なのに同人誌の項目が null」といった不整合な状態を扱わずに済む。
 *
 * 全書籍が共通で持つ情報(タイトル・著者など)は親側に置く。
 */
sealed class BookUiModel {
    abstract val id: Long
    abstract val bookshelfId: Long
    abstract val title: String
    abstract val subtitle: String?
    abstract val coverImageUrl: String?
    abstract val readingStatus: ReadingStatus

    /** 著者名のリスト。同人誌でも作家として複数名を持ちうる。 */
    abstract val authors: List<String>

    /** シリーズ名のリスト。 */
    abstract val seriesNames: List<String>

    /**
     * リスト表示などで 1 行に出すための著者表記。
     * 商業誌は著者名を連結し、同人誌はサークル名を用いる。
     */
    val displayAuthor: String
        get() = when (this) {
            is Commercial -> authors.joinToString("、")
            is Doujin -> circleName.orEmpty()
        }

    /** 商業出版の書籍。 */
    data class Commercial(
        override val id: Long,
        override val bookshelfId: Long,
        override val title: String,
        override val subtitle: String?,
        override val coverImageUrl: String?,
        override val readingStatus: ReadingStatus,
        override val authors: List<String>,
        override val seriesNames: List<String>,
        val isbn: String?,
        val publisher: String?,
    ) : BookUiModel()

    /** 同人出版の書籍。 */
    data class Doujin(
        override val id: Long,
        override val bookshelfId: Long,
        override val title: String,
        override val subtitle: String?,
        override val coverImageUrl: String?,
        override val readingStatus: ReadingStatus,
        override val authors: List<String>,
        override val seriesNames: List<String>,
        val circleId: Long?,
        val circleName: String?,
        val isdn: String?,
    ) : BookUiModel()
}

/**
 * Room の関連モデルを、画面表示用の [BookUiModel] へ変換する。
 *
 * [BookType] によって [BookUiModel.Commercial] / [BookUiModel.Doujin] を出し分け、
 * 詳細テーブルが欠けている場合は各固有項目を null として扱う。
 */
fun BookWithDetailAndAuthors.toUiModel(): BookUiModel {
    val authorNames = authors.map(Author::name)
    val seriesNameList = series.map(Series::name)
    return when (book.bookType) {
        BookType.COMMERCIAL -> BookUiModel.Commercial(
            id = book.id,
            bookshelfId = book.bookshelfId,
            title = book.title,
            subtitle = book.subtitle,
            coverImageUrl = book.thumbnailPath,
            readingStatus = book.readingStatus,
            authors = authorNames,
            seriesNames = seriesNameList,
            isbn = commercialDetail?.isbn,
            publisher = commercialDetail?.publisher,
        )

        BookType.DOUJIN -> BookUiModel.Doujin(
            id = book.id,
            bookshelfId = book.bookshelfId,
            title = book.title,
            subtitle = book.subtitle,
            coverImageUrl = book.thumbnailPath,
            readingStatus = book.readingStatus,
            authors = authorNames,
            seriesNames = seriesNameList,
            circleId = doujinDetail?.detail?.circleId,
            circleName = doujinDetail?.circle?.name,
            isdn = doujinDetail?.detail?.isdn,
        )
    }
}
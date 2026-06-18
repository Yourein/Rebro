package net.yourein.rebro.usecase

import net.yourein.rebro.interfaces.BookshelfRepository
import net.yourein.rebro.model.entity.Bookshelf

/**
 * 本棚に関するユースケース。
 *
 * 書籍詳細から本棚へ辿るような、本棚を起点としない参照もここに集約する。
 */
class BookshelfUseCase(
    private val bookshelfRepository: BookshelfRepository,
) {
    /**
     * 指定 ID の書籍が属する本棚を取得する。
     *
     * 本棚情報を [net.yourein.rebro.model.uimodel.BookUiModel] に持たせると冗長になるため、
     * 書籍 ID からの逆引きとして本棚を取得する。該当が無ければ null を返す。
     */
    suspend fun getBookshelfByBook(bookId: Long): Bookshelf? =
        bookshelfRepository.getBookshelfByBook(bookId)
}

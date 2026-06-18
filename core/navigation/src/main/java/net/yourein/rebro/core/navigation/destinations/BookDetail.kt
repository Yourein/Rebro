package net.yourein.rebro.core.navigation.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 書籍詳細画面 への遷移先。
 *
 * @param bookId 表示する書籍の ID。
 */
@Serializable
data class BookDetail(val bookId: Long) : NavKey

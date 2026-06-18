package net.yourein.rebro.core.navigation.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 本棚詳細画面 への遷移先。
 *
 * @param bookshelfId 表示する本棚の ID。
 */
@Serializable
data class BookshelfDetail(val bookshelfId: Long) : NavKey

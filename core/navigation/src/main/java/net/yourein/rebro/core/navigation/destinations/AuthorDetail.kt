package net.yourein.rebro.core.navigation.destinations

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 著者詳細（著者作品一覧）画面 への遷移先。
 *
 * 書籍詳細などから著者名で遷移する。BookUiModel が著者を名前でしか保持しないため、
 * ここでも著者名で受け渡す。
 *
 * @param authorName 表示する著者の名前。
 */
@Serializable
data class AuthorDetail(val authorName: String) : NavKey

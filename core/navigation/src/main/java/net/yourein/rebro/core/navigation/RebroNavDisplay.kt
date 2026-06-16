package net.yourein.rebro.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

/**
 * アプリ全体のナビゲーションを担う NavHost。
 *
 * Navigation3 の [NavDisplay] をラップし、[NavKey] と各画面の対応付けをここに集約する。
 * feature モジュールは画面 Composable のみを提供し、[NavKey] への依存を持たない
 * （遷移はコールバックで受け取る）。画面間の依存を断つため、対応付けは本モジュールに閉じる。
 *
 * 各 feature の画面が実装され次第、プレースホルダを実画面に差し替えていく。
 *
 * @param backStack 表示するバックスタック。既定では [SearchTop] を起点に生成する。
 */
@Composable
fun RebroNavDisplay(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(SearchTop),
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SearchTop> { PlaceholderScreen("SearchTop") }
            entry<RegisterTop> { PlaceholderScreen("RegisterTop") }
            entry<Bookshelfs> { PlaceholderScreen("Bookshelfs") }
            entry<Authors> { PlaceholderScreen("Authors") }
            entry<BookshelfDetail> { PlaceholderScreen("BookshelfDetail") }
            entry<BookDetail> { PlaceholderScreen("BookDetail") }
            entry<AuthorDetail> { PlaceholderScreen("AuthorDetail") }
        },
    )
}

/**
 * feature 画面が未実装の destination 用の仮表示。
 * 実画面の実装後に該当 entry を差し替えて削除する。
 */
@Composable
private fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}

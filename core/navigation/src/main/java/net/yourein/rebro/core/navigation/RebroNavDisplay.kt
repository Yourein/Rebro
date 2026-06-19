package net.yourein.rebro.core.navigation

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.yourein.rebro.core.navigation.destinations.AllAuthors
import net.yourein.rebro.core.navigation.destinations.AllBooks
import net.yourein.rebro.core.navigation.destinations.AllBookshelves
import net.yourein.rebro.core.navigation.destinations.AuthorDetail
import net.yourein.rebro.core.navigation.destinations.Authors
import net.yourein.rebro.core.navigation.destinations.BookDetail
import net.yourein.rebro.core.navigation.destinations.BookshelfDetail
import net.yourein.rebro.core.navigation.destinations.Bookshelfs
import net.yourein.rebro.core.navigation.destinations.RegisterTop
import net.yourein.rebro.core.navigation.destinations.Search
import net.yourein.rebro.core.navigation.destinations.SearchTop
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.feature.bookdetail.BookDetailScreen
import net.yourein.rebro.feature.registertop.RegisterTopScreen
import net.yourein.rebro.feature.search.SearchScreen
import net.yourein.rebro.feature.searchtop.SearchTopScreen

/**
 * ボトムナビゲーションで相互に行き来できるトップレベルの遷移先。
 *
 * これらの destination を表示している間だけボトムナビゲーションを表示する。
 *
 * @param route 対応する [NavKey]。
 * @param label ナビゲーションアイテムに表示するラベル。
 * @param icon ナビゲーションアイテムに表示するアイコンの drawable リソース。
 */
private enum class TopLevelDestination(
    val route: NavKey,
    val label: String,
    @param:DrawableRes val icon: Int,
) {
    Search(SearchTop, "Search", DrawableR.search_36dp_fill),
    Add(RegisterTop, "Add", DrawableR.library_add_36dp_fill),
}

/**
 * アプリ全体のナビゲーションを担う NavHost。
 *
 * Navigation3 の [NavDisplay] をラップし、[NavKey] と各画面の対応付けをここに集約する。
 * feature モジュールは画面 Composable のみを提供し、[NavKey] への依存を持たない
 * （遷移はコールバックで受け取る）。画面間の依存を断つため、対応付けは本モジュールに閉じる。
 *
 * トップレベル（[TopLevelDestination]）を表示している間はボトムナビゲーションを表示し、
 * 各タブで [SearchTop] と [RegisterTop] を相互に行き来できる。詳細画面など、それ以外の
 * destination ではボトムナビゲーションは隠す。
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
    val currentKey = backStack.lastOrNull()
    Scaffold(
        bottomBar = {
            if (TopLevelDestination.entries.any { it.route == currentKey }) {
                RebroBottomBar(
                    currentKey = currentKey,
                    onDestinationSelected = { route ->
                        if (currentKey != route) {
                            backStack.clear()
                            backStack.add(route)
                        }
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = { navFadeTransition() },
            popTransitionSpec = { navFadeTransition() },
            predictivePopTransitionSpec = { navFadeTransition() },
            entryProvider = entryProvider {
                entry<SearchTop> {
                    SearchTopScreen(
                        navigateToSearchScreen = { backStack.add(Search) },
                        navigateToAllBooks = { backStack.add(AllBooks) },
                        navigateToAllBookshelves = { backStack.add(AllBookshelves) },
                        navigateToAllAuthors = { backStack.add(AllAuthors) },
                        navigateToBookDetail = { bookId -> backStack.add(BookDetail(bookId)) },
                    )
                }
                entry<RegisterTop> { RegisterTopScreen() }
                entry<Bookshelfs> { PlaceholderScreen("Bookshelfs") }
                entry<Authors> { PlaceholderScreen("Authors") }
                entry<BookshelfDetail> { key -> PlaceholderScreen("BookshelfDetail(bookshelfId=${key.bookshelfId})") }
                entry<BookDetail> { key ->
                    Log.d("yourein", "$key")
                    BookDetailScreen(
                        bookId = key.bookId,
                        navigateBack = { backStack.removeLastOrNull() },
                        navigateToAuthorDetail = { authorName -> backStack.add(AuthorDetail(authorName)) },
                        navigateToBookshelfDetail = { bookshelfId -> backStack.add(BookshelfDetail(bookshelfId)) },
                    )
                }
                entry<AuthorDetail> { key -> PlaceholderScreen("AuthorDetail(authorName=${key.authorName})") }
                entry<AllBooks> { PlaceholderScreen("AllBooks") }
                entry<AllBookshelves> { PlaceholderScreen("AllBookshelves") }
                entry<AllAuthors> { PlaceholderScreen("AllAuthors") }
                entry<Search> {
                    SearchScreen(
                        navigateToBookDetail = { bookId -> backStack.add(BookDetail(bookId)) },
                    )
                }
            },
        )
    }
}

/**
 * トップレベルの遷移先を切り替えるボトムナビゲーション。
 *
 * backStack の操作は持たず、選択されたタブの [NavKey] を [onDestinationSelected] で通知するに留める。
 *
 * @param currentKey 現在表示中の [NavKey]。選択中タブのハイライトに用いる。
 * @param onDestinationSelected タブが選択されたときに、その遷移先の [NavKey] を渡して呼ばれる。
 */
@Composable
private fun RebroBottomBar(
    currentKey: NavKey?,
    onDestinationSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = RebroColor.Surface,
        modifier = modifier
            .windowInsetsPadding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
    ) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentKey == destination.route,
                onClick = { onDestinationSelected(destination.route) },
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = destination.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = RebroColor.Brand.copy(alpha = 0.3f),
                    unselectedIconColor = RebroColor.TextSecondary,
                    unselectedTextColor = RebroColor.TextSecondary,
                ),
            )
        }
    }
}

/** 画面遷移のフェードに用いる時間（ミリ秒）。既定より短くして遷移を素早く見せる。 */
private const val NavTransitionDurationMillis = 200

/**
 * [NavDisplay] の各遷移で共通利用する、短いクロスフェード。
 * 入り・出ともに [NavTransitionDurationMillis] の [tween] で揃える。
 */
private fun navFadeTransition(): ContentTransform =
    ContentTransform(
        targetContentEnter = fadeIn(tween(NavTransitionDurationMillis)),
        initialContentExit = fadeOut(tween(NavTransitionDurationMillis)),
        sizeTransform = SizeTransform { _, _ -> tween(NavTransitionDurationMillis) },
    )

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

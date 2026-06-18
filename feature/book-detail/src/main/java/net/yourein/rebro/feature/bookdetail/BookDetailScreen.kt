package net.yourein.rebro.feature.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.core.resources.RebroTheme
import net.yourein.rebro.model.ReadingStatus
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.uimodel.BookUiModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookDetailScreen(
    bookId: Long,
    navigateBack: () -> Unit,
    navigateToAuthorDetail: (authorName: String) -> Unit,
    navigateToBookshelfDetail: (bookshelfId: Long) -> Unit,
    viewModel: BookDetailViewModel = koinViewModel { parametersOf(bookId) },
) {
    BookDetailScreen(
        bookState = viewModel.bookState,
        bookshelf = viewModel.bookshelf,
        navigateBack = navigateBack,
        navigateToAuthorDetail = navigateToAuthorDetail,
        navigateToBookshelfDetail = navigateToBookshelfDetail,
    )
}

@Composable
private fun BookDetailScreen(
    bookState: LoadingState<BookUiModel>,
    bookshelf: Bookshelf?,
    navigateBack: () -> Unit,
    navigateToAuthorDetail: (authorName: String) -> Unit,
    navigateToBookshelfDetail: (bookshelfId: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
    ) {
        BookDetailTopBar(
            onBackClick = navigateBack,
            modifier = Modifier
                .background(RebroColor.Background)
                .fillMaxWidth()
        )

        when (bookState) {
            is LoadingState.Success -> {
                BookDetailSuccessScreen(
                    book = bookState.value,
                    bookshelf = bookshelf,
                    navigateToAuthorDetail = navigateToAuthorDetail,
                    navigateToBookshelfDetail = navigateToBookshelfDetail,
                )
            }

            is LoadingState.Loading -> {
                BookDetailLoadingScreen()
            }

            is LoadingState.Error -> {
                BookDetailErrorScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenCommercialPreview() {
    RebroTheme {
        BookDetailScreen(
            bookState = LoadingState.Success(
                BookUiModel.Commercial(
                    id = 1L,
                    bookshelfId = 1L,
                    title = "吾輩は猫である",
                    subtitle = "上巻",
                    coverImageUrl = null,
                    readingStatus = ReadingStatus.READING,
                    authors = listOf("夏目漱石"),
                    isbn = "978-4-00-310101-8",
                    publisher = "岩波書店",
                )
            ),
            bookshelf = Bookshelf(id = 1L, name = "文学"),
            navigateBack = {},
            navigateToAuthorDetail = {},
            navigateToBookshelfDetail = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenDoujinPreview() {
    RebroTheme {
        BookDetailScreen(
            bookState = LoadingState.Success(
                BookUiModel.Doujin(
                    id = 2L,
                    bookshelfId = 1L,
                    title = "とある同人誌",
                    subtitle = null,
                    coverImageUrl = null,
                    readingStatus = ReadingStatus.UNREAD,
                    authors = listOf("作家A", "作家B"),
                    circleId = 1L,
                    circleName = "サークル名",
                    isdn = "ISDN-0000-0000",
                )
            ),
            bookshelf = Bookshelf(id = 1L, name = "同人誌"),
            navigateBack = {},
            navigateToAuthorDetail = {},
            navigateToBookshelfDetail = {},
        )
    }
}

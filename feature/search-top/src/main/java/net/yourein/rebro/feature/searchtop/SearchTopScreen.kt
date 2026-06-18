package net.yourein.rebro.feature.searchtop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.yourein.rebro.core.compose.BookListItem
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.core.resources.RebroTheme
import net.yourein.rebro.model.uimodel.BookUiModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchTopScreen(
    navigateToSearchScreen: () -> Unit,
    navigateToAllBooks: () -> Unit,
    navigateToAllBookshelves: () -> Unit,
    navigateToAllAuthors: () -> Unit,
    navigateToBookDetail: (bookId: Long) -> Unit,
    viewModel: SearchTopViewModel = koinViewModel(),
) {
    val recentBooks by viewModel.recentBooksState.collectAsStateWithLifecycle()
    SearchTopScreen(
        recentBooksState = recentBooks,
        navigateToSearchScreen = navigateToSearchScreen,
        navigateToAllBooks = navigateToAllBooks,
        navigateToAllBookshelves = navigateToAllBookshelves,
        navigateToAllAuthors = navigateToAllAuthors,
        navigateToBookDetail = navigateToBookDetail,
    )
}

@Composable
fun SearchTopScreen(
    recentBooksState: LoadingState<List<BookUiModel>>,
    navigateToSearchScreen: () -> Unit,
    navigateToAllBooks: () -> Unit,
    navigateToAllBookshelves: () -> Unit,
    navigateToAllAuthors: () -> Unit,
    navigateToBookDetail: (bookId: Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchTopBar(
            onClick = navigateToSearchScreen,
            modifier = Modifier
                .background(RebroColor.Background)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth()
        )

        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.size(8.dp))
                NavigatorItem(
                    text = "Books",
                    icon = painterResource(DrawableR.book_4_24dp_fill),
                    onClick = navigateToAllBooks,
                )
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                NavigatorItem(
                    text = "Bookshelves",
                    icon = painterResource(DrawableR.shelves_24dp_fill),
                    onClick = navigateToAllBookshelves,
                )
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                NavigatorItem(
                    text = "Authors",
                    icon = painterResource(DrawableR.article_person_24dp_fill),
                    onClick = navigateToAllAuthors,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }

            stickyHeader {
                Text(
                    text = "Recently Added Books",
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(RebroColor.Background)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when(recentBooksState) {
                is LoadingState.Success -> {
                    val books = recentBooksState.value
                    if (books.isEmpty()) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Text(
                                    text = "No Recent Books."
                                )
                            }
                        }
                    } else {
                        items(books) { book ->
                            val subtitle = book.subtitle
                            val displaySubtitle = if (!subtitle.isNullOrEmpty()) {
                                " $subtitle"
                            } else {
                                ""
                            }
                            BookListItem(
                                title = book.title + displaySubtitle,
                                author = book.displayAuthor,
                                coverImageUrl = book.coverImageUrl,
                                onClick = { navigateToBookDetail(book.id) },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                            )

                            if (book != books.last()) {
                                HorizontalDivider(
                                    color = Color.Gray,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
                is LoadingState.Error -> {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "Error!"
                            )
                        }
                    }
                }
                is LoadingState.Loading -> {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NavigatorItem(
    text: String,
    icon: Painter? = null,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.size(8.dp))
        }

        Text(
            text = text,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Icon(
            painter = painterResource(DrawableR.chevron_forward_24dp_fill),
            contentDescription = null,
        )
    }
}

@Composable
internal fun SearchTopBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(DrawableR.search_36dp_fill),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "Type Title or ISDN(ISBN) to search",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchTopBarPreview() {
    RebroTheme {
        SearchTopBar(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
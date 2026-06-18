package net.yourein.rebro.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
fun SearchScreen(
    navigateToBookDetail: (bookId: Long) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResultState by viewModel.searchResultState.collectAsStateWithLifecycle()
    SearchScreen(
        query = query,
        searchResultState = searchResultState,
        onQueryChange = viewModel::onQueryChange,
        navigateToBookDetail = navigateToBookDetail,
    )
}

@Composable
fun SearchScreen(
    query: String,
    searchResultState: LoadingState<List<BookUiModel>>,
    onQueryChange: (String) -> Unit,
    navigateToBookDetail: (bookId: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .background(RebroColor.Background)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .fillMaxWidth()
        )

        val bottomContentPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomContentPadding),
            modifier = Modifier.fillMaxSize()
        ) {
            when (searchResultState) {
                is LoadingState.Success -> {
                    val books = searchResultState.value
                    if (books.isEmpty()) {
                        item {
                            CenteredMessage(
                                text = if (query.isBlank()) {
                                    "Type Title or ISDN(ISBN) to search."
                                } else {
                                    "No books found."
                                }
                            )
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
                                modifier = Modifier.fillMaxWidth()
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
                    item { CenteredMessage(text = "Error!") }
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
private fun CenteredMessage(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(text = text)
    }
}

@Composable
internal fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 画面表示直後に検索欄へフォーカスを当て、ソフトウェアキーボードを開く。
    // requestFocus だけでは端末によってキーボードが出ないことがあるため、明示的に show() も呼ぶ。
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(DrawableR.search_36dp_fill),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(Color.Black),
            textStyle = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Type Title or ISDN(ISBN) to search",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = Color.Gray,
                    )
                }
                innerTextField()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    RebroTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

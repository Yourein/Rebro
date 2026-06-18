package net.yourein.rebro.feature.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.yourein.rebro.core.compose.LoadingState
import net.yourein.rebro.core.resources.DrawableR
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
                BookDetailContent(
                    book = bookState.value,
                    bookshelf = bookshelf,
                    navigateToAuthorDetail = navigateToAuthorDetail,
                    navigateToBookshelfDetail = navigateToBookshelfDetail,
                )
            }

            is LoadingState.Loading -> {
                CenteredBox { CircularProgressIndicator() }
            }

            is LoadingState.Error -> {
                CenteredBox { Text(text = "Failed to load this book.") }
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: BookUiModel,
    bookshelf: Bookshelf?,
    navigateToAuthorDetail: (authorName: String) -> Unit,
    navigateToBookshelfDetail: (bookshelfId: Long) -> Unit,
) {
    val bottomPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding + 16.dp)
    ) {
        AsyncImage(
            model = book.coverImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(300.dp)
                .aspectRatio(COVER_ASPECT_RATIO)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.size(24.dp))

        Text(
            text = book.title,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold,
            color = RebroColor.TextPrimary,
        )

        val subtitle = book.subtitle
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = RebroColor.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.size(24.dp))

        AuthorsField(
            authors = book.authors,
            onAuthorClick = navigateToAuthorDetail,
        )

        if (bookshelf != null) {
            DetailDivider()
            LinkField(
                label = "Bookshelf",
                value = bookshelf.name,
                onClick = { navigateToBookshelfDetail(bookshelf.id) },
            )
        }

        DetailDivider()

        DetailField(label = "Reading Status", value = book.readingStatus.label)

        when (book) {
            is BookUiModel.Commercial -> {
                DetailDivider()
                DetailField(label = "Publisher", value = book.publisher.orPlaceholder())
                DetailDivider()
                DetailField(label = "ISBN", value = book.isbn.orPlaceholder())
            }

            is BookUiModel.Doujin -> {
                DetailDivider()
                DetailField(label = "Circle", value = book.circleName.orPlaceholder())
                DetailDivider()
                DetailField(label = "ISDN", value = book.isdn.orPlaceholder())
            }
        }
    }
}

/**
 * 著者の一覧。各著者名は、その著者の作品一覧へ遷移できるリンクとして表示する。
 */
@Composable
private fun AuthorsField(
    authors: List<String>,
    onAuthorClick: (authorName: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(text = "Author")
        if (authors.isEmpty()) {
            Text(
                text = PLACEHOLDER,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = RebroColor.TextPrimary,
            )
        } else {
            authors.forEach { author ->
                LinkText(
                    text = author,
                    onClick = { onAuthorClick(author) },
                )
            }
        }
    }
}

/**
 * ラベルと、遷移リンクとして振る舞う値を縦に並べたフィールド。
 */
@Composable
private fun LinkField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(text = label)
        LinkText(text = value, onClick = onClick)
    }
}

/** 別画面へ遷移できることを示す、強調色のクリック可能なテキスト。 */
@Composable
private fun LinkText(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        color = RebroColor.Highlight,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun DetailField(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(text = label)
        Text(
            text = value,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = RebroColor.TextPrimary,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = RebroColor.TextSecondary,
    )
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color = Color.Gray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun BookDetailTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(DrawableR.chevron_forward_24dp_fill),
            contentDescription = "Back",
            tint = RebroColor.TextPrimary,
            // chevron_forward を左右反転し、戻る向きの矢印として用いる。
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .clickable { onBackClick() }
                .padding(8.dp)
                .size(24.dp)
                .scale(scaleX = -1f, scaleY = 1f)
        )
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        content()
    }
}

/** 書影の縦横比(横 : 縦 = 2 : 3 を想定した一般的な書籍カバー比率)。 */
private const val COVER_ASPECT_RATIO = 2f / 3f

/** 値が未設定のときに表示するプレースホルダ。 */
private const val PLACEHOLDER = "—"

private fun String?.orPlaceholder(): String =
    if (isNullOrBlank()) PLACEHOLDER else this

/** 読書状態を画面表示用のラベルへ変換する。 */
private val ReadingStatus.label: String
    get() = when (this) {
        ReadingStatus.UNREAD -> "Unread"
        ReadingStatus.READING -> "Reading"
        ReadingStatus.COMPLETED -> "Completed"
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

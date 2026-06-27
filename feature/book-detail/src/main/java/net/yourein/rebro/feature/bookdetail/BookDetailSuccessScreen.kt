package net.yourein.rebro.feature.bookdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.yourein.rebro.core.compose.AuthorSelectionSection
import net.yourein.rebro.core.compose.BookshelfSelectionSection
import net.yourein.rebro.core.compose.CircleSelectionSection
import net.yourein.rebro.core.compose.SeriesSelectionSection
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.model.ReadingStatus
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.Series
import net.yourein.rebro.model.uimodel.BookUiModel

@Composable
internal fun BookDetailSuccessScreen(
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
                .sizeIn(maxWidth = 300.dp, maxHeight = 300.dp)
                .fillMaxSize()
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
            HorizontalDivider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            FieldLabel(text = "Bookshelf")
            LinkText(
                text = bookshelf.name,
                onClick = { navigateToBookshelfDetail(bookshelf.id) }
            )
        }

        HorizontalDivider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (book.seriesNames.isNotEmpty()) {
            SeriesField(seriesNames = book.seriesNames)
            HorizontalDivider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        DetailField(label = "Reading Status", value = book.readingStatus.label)

        when (book) {
            is BookUiModel.Commercial -> {
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                DetailField(label = "Publisher", value = book.publisher.orPlaceholder())
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                DetailField(label = "ISBN", value = book.isbn.orPlaceholder())
            }

            is BookUiModel.Doujin -> {
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                DetailField(label = "Circle", value = book.circleName.orPlaceholder())
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                DetailField(label = "ISDN", value = book.isdn.orPlaceholder())
            }
        }
    }
}

// ── 編集モード ────────────────────────────────────

@Composable
internal fun BookDetailEditScreen(
    book: BookUiModel,
    editTitle: String,
    editSubtitle: String,
    editReadingStatus: ReadingStatus,
    editPublisher: String,
    editIsbn: String,
    editIsdn: String,
    editSelectedAuthors: List<Author>,
    allAuthors: List<Author>,
    editSelectedBookshelf: Bookshelf?,
    allBookshelves: List<Bookshelf>,
    editSelectedCircle: Circle?,
    allCircles: List<Circle>,
    editSelectedSeries: List<Series>,
    allSeries: List<Series>,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onReadingStatusChange: (ReadingStatus) -> Unit,
    onPublisherChange: (String) -> Unit,
    onIsbnChange: (String) -> Unit,
    onIsdnChange: (String) -> Unit,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
    onRenameAuthor: (Long, String) -> Unit,
    onSetBookshelf: (Bookshelf?) -> Unit,
    onAddNewBookshelf: (String) -> Unit,
    onSetCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
    onRenameCircle: (Long, String) -> Unit,
    onToggleSeries: (Series) -> Unit,
    onRemoveSeries: (Series) -> Unit,
    onAddNewSeries: (String) -> Unit,
    onSave: () -> Unit,
) {
    val bottomPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                .sizeIn(maxWidth = 300.dp, maxHeight = 300.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.size(12.dp))

        OutlinedTextField(
            value = editTitle,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = editSubtitle,
            onValueChange = onSubtitleChange,
            label = { Text("Subtitle") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        AuthorSelectionSection(
            selectedAuthors = editSelectedAuthors,
            allAuthors = allAuthors,
            onToggleAuthor = onToggleAuthor,
            onRemoveAuthor = onRemoveAuthor,
            onAddNewAuthor = onAddNewAuthor,
            onRenameAuthor = onRenameAuthor,
        )

        HorizontalDivider()

        SeriesSelectionSection(
            selectedSeries = editSelectedSeries,
            allSeries = allSeries,
            onToggleSeries = onToggleSeries,
            onRemoveSeries = onRemoveSeries,
            onAddNewSeries = onAddNewSeries,
        )

        HorizontalDivider()

        BookshelfSelectionSection(
            selectedBookshelf = editSelectedBookshelf,
            allBookshelves = allBookshelves,
            onSelectBookshelf = onSetBookshelf,
            onAddNewBookshelf = onAddNewBookshelf,
        )

        HorizontalDivider()

        Text(text = "Reading Status", fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReadingStatus.entries.forEach { status ->
                FilterChip(
                    selected = editReadingStatus == status,
                    onClick = { onReadingStatusChange(status) },
                    label = { Text(status.label) },
                )
            }
        }

        HorizontalDivider()

        when (book) {
            is BookUiModel.Commercial -> {
                OutlinedTextField(
                    value = editPublisher,
                    onValueChange = onPublisherChange,
                    label = { Text("Publisher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = editIsbn,
                    onValueChange = onIsbnChange,
                    label = { Text("ISBN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is BookUiModel.Doujin -> {
                CircleSelectionSection(
                    selectedCircle = editSelectedCircle,
                    allCircles = allCircles,
                    onSelectCircle = onSetCircle,
                    onAddNewCircle = onAddNewCircle,
                    onRenameCircle = onRenameCircle,
                )

                OutlinedTextField(
                    value = editIsdn,
                    onValueChange = onIsdnChange,
                    label = { Text("ISDN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HorizontalDivider()

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }
    }
}

// ── 共通コンポーネント ──────────────────────────────

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

@Composable
private fun SeriesField(seriesNames: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(text = "Series")
        seriesNames.forEach { name ->
            Text(
                text = name,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = RebroColor.TextPrimary,
            )
        }
    }
}

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
internal fun DetailField(
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
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = RebroColor.TextSecondary,
    )
}

private const val PLACEHOLDER = "—"

internal fun String?.orPlaceholder(): String =
    if (isNullOrBlank()) PLACEHOLDER else this

internal val ReadingStatus.label: String
    get() = when (this) {
        ReadingStatus.UNREAD -> "Unread"
        ReadingStatus.READING -> "Reading"
        ReadingStatus.COMPLETED -> "Completed"
    }

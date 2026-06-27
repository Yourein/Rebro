package net.yourein.rebro.feature.bookdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.yourein.rebro.core.compose.AuthorSelectionSection
import net.yourein.rebro.core.compose.BookshelfSelectionSection
import net.yourein.rebro.core.compose.CircleSelectionSection
import net.yourein.rebro.core.compose.SeriesSelectionSection
import net.yourein.rebro.core.resources.DrawableR
import net.yourein.rebro.core.resources.RebroColor
import net.yourein.rebro.model.ReadingStatus
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.Series
import net.yourein.rebro.model.uimodel.BookUiModel
import java.io.File

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
    coverImagePath: String?,
    isDownloading: Boolean,
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
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
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
        CoverImageEditSection(
            coverImagePath = coverImagePath,
            isDownloading = isDownloading,
            onPickFromGallery = onPickFromGallery,
            onUrlSpecified = onUrlSpecified,
            onClearImage = onClearImage,
        )

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

// ── カバー画像編集セクション ────────────────────────

@Composable
private fun CoverImageEditSection(
    coverImagePath: String?,
    isDownloading: Boolean,
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
) {
    var showUrlDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Cover Image", fontSize = 16.sp)

        if (isDownloading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
            ) {
                CircularProgressIndicator()
            }
        } else if (coverImagePath != null) {
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AsyncImage(
                    model = File(coverImagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                IconButton(onClick = onClearImage) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove cover image",
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onPickFromGallery() },
                ) {
                    Icon(
                        painter = painterResource(DrawableR.add_photo_alternate_24dp_fill),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload from\nLocal Storage",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.size(60.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showUrlDialog = true },
                ) {
                    Icon(
                        painter = painterResource(DrawableR.link_24dp_fill),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Specify\nImage URL",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    if (showUrlDialog) {
        ImageUrlDialog(
            onDismiss = { showUrlDialog = false },
            onConfirm = { url ->
                showUrlDialog = false
                onUrlSpecified(url)
            },
        )
    }
}

@Composable
private fun ImageUrlDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image URL") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank(),
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

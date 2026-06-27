package net.yourein.rebro.feature.registertop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.yourein.rebro.core.compose.AuthorSelectionSection
import net.yourein.rebro.core.compose.BookshelfSelectionSection
import net.yourein.rebro.core.compose.CircleSelectionSection
import net.yourein.rebro.core.compose.SeriesSelectionSection
import net.yourein.rebro.core.resources.RebroTheme
import net.yourein.rebro.model.BookType
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.Series
import org.koin.androidx.compose.koinViewModel

/** 書籍登録画面。 */
@Composable
fun RegisterTopScreen(
    navigateToQrScan: () -> Unit = {},
    navigateToIsdnDebug: () -> Unit = {},
    pendingAutofill: AutofillResult? = null,
    onAutofillConsumed: () -> Unit = {},
    viewModel: RegisterTopViewModel = koinViewModel(),
) {
    LaunchedEffect(pendingAutofill) {
        if (pendingAutofill != null) {
            viewModel.applyAutofill(pendingAutofill)
            onAutofillConsumed()
        }
    }

    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val subtitle by viewModel.subtitle.collectAsStateWithLifecycle()
    val bookType by viewModel.bookType.collectAsStateWithLifecycle()
    val publisher by viewModel.publisher.collectAsStateWithLifecycle()
    val coverImagePath by viewModel.coverImagePath.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val selectedBookshelf by viewModel.selectedBookshelf.collectAsStateWithLifecycle()
    val allBookshelves by viewModel.allBookshelves.collectAsStateWithLifecycle()
    val selectedAuthors by viewModel.selectedAuthors.collectAsStateWithLifecycle()
    val allAuthors by viewModel.allAuthors.collectAsStateWithLifecycle()
    val selectedCircle by viewModel.selectedCircle.collectAsStateWithLifecycle()
    val allCircles by viewModel.allCircles.collectAsStateWithLifecycle()
    val selectedSeries by viewModel.selectedSeries.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.saveCoverImageFromPicker(it) }
    }

    RegisterTopScreen(
        navigateToQrScan = navigateToQrScan,
        navigateToIsdnDebug = navigateToIsdnDebug,
        lastResult = lastResult,
        title = title,
        subtitle = subtitle,
        bookType = bookType,
        publisher = publisher,
        coverImagePath = coverImagePath,
        isDownloading = isDownloading,
        selectedBookshelf = selectedBookshelf,
        allBookshelves = allBookshelves,
        selectedAuthors = selectedAuthors,
        allAuthors = allAuthors,
        selectedCircle = selectedCircle,
        allCircles = allCircles,
        selectedSeries = selectedSeries,
        allSeries = allSeries,
        onTitleChange = viewModel::setTitle,
        onSubtitleChange = viewModel::setSubtitle,
        onBookTypeChange = viewModel::setBookType,
        onPublisherChange = viewModel::setPublisher,
        onRegister = viewModel::registerBook,
        onPickFromGallery = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onUrlSpecified = viewModel::downloadCoverImage,
        onClearImage = viewModel::clearCoverImage,
        onSetBookshelf = viewModel::setBookshelf,
        onAddNewBookshelf = viewModel::addNewBookshelf,
        onToggleAuthor = viewModel::toggleAuthor,
        onRemoveAuthor = viewModel::removeAuthor,
        onAddNewAuthor = viewModel::addNewAuthor,
        onRenameAuthor = viewModel::renameAuthor,
        onSetCircle = viewModel::setCircle,
        onAddNewCircle = viewModel::addNewCircle,
        onRenameCircle = viewModel::renameCircle,
        onToggleSeries = viewModel::toggleSeries,
        onRemoveSeries = viewModel::removeSeries,
        onAddNewSeries = viewModel::addNewSeries,
    )
}

@Composable
internal fun RegisterTopScreen(
    navigateToQrScan: () -> Unit,
    navigateToIsdnDebug: () -> Unit,
    lastResult: String?,
    title: String,
    subtitle: String,
    bookType: BookType,
    publisher: String,
    coverImagePath: String?,
    isDownloading: Boolean,
    selectedBookshelf: Bookshelf?,
    allBookshelves: List<Bookshelf>,
    selectedAuthors: List<Author>,
    allAuthors: List<Author>,
    selectedCircle: Circle?,
    allCircles: List<Circle>,
    selectedSeries: List<Series>,
    allSeries: List<Series>,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onBookTypeChange: (BookType) -> Unit,
    onPublisherChange: (String) -> Unit,
    onRegister: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
    onSetBookshelf: (Bookshelf?) -> Unit,
    onAddNewBookshelf: (String) -> Unit,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
    onRenameAuthor: (Long, String) -> Unit,
    onSetCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
    onRenameCircle: (Long, String) -> Unit,
    onToggleSeries: (Series) -> Unit,
    onRemoveSeries: (Series) -> Unit,
    onAddNewSeries: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
            .padding(16.dp),
    ) {
        Text(
            text = "Register A New Book",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

        HorizontalDivider()

        RegisterTopAutofillSection(
            onNavigateToQrScan = navigateToQrScan,
            onNavigateToIsdnDebug = navigateToIsdnDebug,
        )

        HorizontalDivider()

        Text(
            text = "Type",
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BookType.entries.forEach { type ->
                FilterChip(
                    selected = bookType == type,
                    onClick = { onBookTypeChange(type) },
                    label = { Text(type.displayLabel) },
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title (Required)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = subtitle,
            onValueChange = onSubtitleChange,
            label = { Text("Subtitle") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        AuthorSelectionSection(
            selectedAuthors = selectedAuthors,
            allAuthors = allAuthors,
            onToggleAuthor = onToggleAuthor,
            onRemoveAuthor = onRemoveAuthor,
            onAddNewAuthor = onAddNewAuthor,
            onRenameAuthor = onRenameAuthor,
        )

        SeriesSelectionSection(
            selectedSeries = selectedSeries,
            allSeries = allSeries,
            onToggleSeries = onToggleSeries,
            onRemoveSeries = onRemoveSeries,
            onAddNewSeries = onAddNewSeries,
        )

        when (bookType) {
            BookType.COMMERCIAL -> {
                OutlinedTextField(
                    value = publisher,
                    onValueChange = onPublisherChange,
                    label = { Text("Publisher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            BookType.DOUJIN -> {
                CircleSelectionSection(
                    selectedCircle = selectedCircle,
                    allCircles = allCircles,
                    onSelectCircle = onSetCircle,
                    onAddNewCircle = onAddNewCircle,
                    onRenameCircle = onRenameCircle,
                )
            }
        }

        BookshelfSelectionSection(
            selectedBookshelf = selectedBookshelf,
            allBookshelves = allBookshelves,
            onSelectBookshelf = onSetBookshelf,
            onAddNewBookshelf = onAddNewBookshelf,
        )

        HorizontalDivider()

        RegisterTopCoverImageSection(
            coverImagePath = coverImagePath,
            isDownloading = isDownloading,
            onPickFromGallery = onPickFromGallery,
            onUrlSpecified = onUrlSpecified,
            onClearImage = onClearImage,
        )

        HorizontalDivider()

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Register")
        }

        lastResult?.let {
            HorizontalDivider()
            Text(text = it, fontSize = 14.sp)
        }
    }
}

private val BookType.displayLabel: String
    get() = when (this) {
        BookType.COMMERCIAL -> "Commercial"
        BookType.DOUJIN -> "Doujinshi (Indie)"
    }

@Preview(showBackground = true)
@Composable
private fun RegisterTopScreenPreview() {
    RebroTheme {
        RegisterTopScreen(
            navigateToQrScan = {},
            navigateToIsdnDebug = {},
            lastResult = "登録しました（bookId=3）：サンプル本 #3",
            title = "",
            subtitle = "",
            bookType = BookType.COMMERCIAL,
            publisher = "",
            coverImagePath = null,
            isDownloading = false,
            selectedBookshelf = Bookshelf(id = 1, name = "デバッグ本棚"),
            allBookshelves = listOf(
                Bookshelf(id = 1, name = "デバッグ本棚"),
            ),
            selectedAuthors = listOf(
                Author(id = 1, name = "テスト著者"),
            ),
            allAuthors = listOf(
                Author(id = 1, name = "テスト著者"),
                Author(id = 2, name = "別の著者"),
            ),
            selectedCircle = null,
            allCircles = emptyList(),
            selectedSeries = emptyList(),
            allSeries = emptyList(),
            onTitleChange = {},
            onSubtitleChange = {},
            onBookTypeChange = {},
            onPublisherChange = {},
            onRegister = {},
            onPickFromGallery = {},
            onUrlSpecified = {},
            onClearImage = {},
            onSetBookshelf = {},
            onAddNewBookshelf = {},
            onToggleAuthor = {},
            onRemoveAuthor = {},
            onAddNewAuthor = {},
            onRenameAuthor = { _, _ -> },
            onSetCircle = {},
            onAddNewCircle = {},
            onRenameCircle = { _, _ -> },
            onToggleSeries = {},
            onRemoveSeries = {},
            onAddNewSeries = {},
        )
    }
}

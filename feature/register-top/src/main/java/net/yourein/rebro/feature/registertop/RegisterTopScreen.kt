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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    navigateToIsdnDebug: () -> Unit = {},
    pendingAutofill: AutofillResult? = null,
    onAutofillConsumed: () -> Unit = {},
    viewModel: RegisterTopViewModel = koinViewModel(),
) {
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val registrationSuccess by viewModel.registrationSuccess.collectAsStateWithLifecycle()
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
        navigateToIsdnDebug = navigateToIsdnDebug,
        lastResult = lastResult,
        registrationSuccess = registrationSuccess,
        coverImagePath = coverImagePath,
        isDownloading = isDownloading,
        pendingAutofill = pendingAutofill,
        selectedBookshelf = selectedBookshelf,
        allBookshelves = allBookshelves,
        selectedAuthors = selectedAuthors,
        allAuthors = allAuthors,
        selectedCircle = selectedCircle,
        allCircles = allCircles,
        selectedSeries = selectedSeries,
        allSeries = allSeries,
        onRegister = viewModel::registerBook,
        onConsumeRegistrationSuccess = viewModel::consumeRegistrationSuccess,
        onConsumeAutofill = { result ->
            viewModel.applyAutofill(result)
            onAutofillConsumed()
        },
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
        onSetCircle = viewModel::setCircle,
        onAddNewCircle = viewModel::addNewCircle,
        onToggleSeries = viewModel::toggleSeries,
        onRemoveSeries = viewModel::removeSeries,
        onAddNewSeries = viewModel::addNewSeries,
    )
}

@Composable
internal fun RegisterTopScreen(
    navigateToIsdnDebug: () -> Unit,
    lastResult: String?,
    registrationSuccess: Boolean,
    coverImagePath: String?,
    isDownloading: Boolean,
    pendingAutofill: AutofillResult?,
    selectedBookshelf: Bookshelf?,
    allBookshelves: List<Bookshelf>,
    selectedAuthors: List<Author>,
    allAuthors: List<Author>,
    selectedCircle: Circle?,
    allCircles: List<Circle>,
    selectedSeries: List<Series>,
    allSeries: List<Series>,
    onRegister: (title: String, subtitle: String, bookType: BookType, publisher: String) -> Unit,
    onConsumeRegistrationSuccess: () -> Unit,
    onConsumeAutofill: (AutofillResult) -> Unit,
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
    onSetBookshelf: (Bookshelf?) -> Unit,
    onAddNewBookshelf: (String) -> Unit,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
    onSetCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
    onToggleSeries: (Series) -> Unit,
    onRemoveSeries: (Series) -> Unit,
    onAddNewSeries: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var bookType by remember { mutableStateOf(BookType.COMMERCIAL) }
    var publisher by remember { mutableStateOf("") }

    LaunchedEffect(pendingAutofill) {
        if (pendingAutofill != null) {
            title = pendingAutofill.title
            bookType = pendingAutofill.bookType
            publisher = pendingAutofill.publisher
            onConsumeAutofill(pendingAutofill)
        }
    }

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            title = ""
            subtitle = ""
            publisher = ""
            onConsumeRegistrationSuccess()
        }
    }

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
                    onClick = { bookType = type },
                    label = { Text(type.displayLabel) },
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title (Required)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
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
                    onValueChange = { publisher = it },
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
            onClick = { onRegister(title, subtitle, bookType, publisher) },
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
            navigateToIsdnDebug = {},
            lastResult = "登録しました（bookId=3）：サンプル本 #3",
            registrationSuccess = false,
            coverImagePath = null,
            isDownloading = false,
            pendingAutofill = null,
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
            onRegister = { _, _, _, _ -> },
            onConsumeRegistrationSuccess = {},
            onConsumeAutofill = {},
            onPickFromGallery = {},
            onUrlSpecified = {},
            onClearImage = {},
            onSetBookshelf = {},
            onAddNewBookshelf = {},
            onToggleAuthor = {},
            onRemoveAuthor = {},
            onAddNewAuthor = {},
            onSetCircle = {},
            onAddNewCircle = {},
            onToggleSeries = {},
            onRemoveSeries = {},
            onAddNewSeries = {},
        )
    }
}

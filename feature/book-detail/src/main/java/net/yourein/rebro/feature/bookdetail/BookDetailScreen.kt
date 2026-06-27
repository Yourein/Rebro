package net.yourein.rebro.feature.bookdetail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val editSelectedAuthors by viewModel.editSelectedAuthors.collectAsStateWithLifecycle()
    val allAuthors by viewModel.allAuthors.collectAsStateWithLifecycle()
    val editSelectedBookshelf by viewModel.editSelectedBookshelf.collectAsStateWithLifecycle()
    val allBookshelves by viewModel.allBookshelves.collectAsStateWithLifecycle()
    val editSelectedCircle by viewModel.editSelectedCircle.collectAsStateWithLifecycle()
    val allCircles by viewModel.allCircles.collectAsStateWithLifecycle()
    val editSelectedSeries by viewModel.editSelectedSeries.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val coverImagePath by viewModel.editCoverImagePath.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.saveCoverImageFromPicker(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
    ) {
        BookDetailTopBar(
            isEditing = viewModel.isEditing,
            onBackClick = navigateBack,
            onEditClick = viewModel::startEditing,
            onCancelEditClick = viewModel::cancelEditing,
            modifier = Modifier
                .background(RebroColor.Background)
                .fillMaxWidth()
        )

        when (val state = viewModel.bookState) {
            is LoadingState.Success -> {
                if (viewModel.isEditing) {
                    BookDetailEditScreen(
                        book = state.value,
                        editTitle = viewModel.editTitle,
                        editSubtitle = viewModel.editSubtitle,
                        editReadingStatus = viewModel.editReadingStatus,
                        editPublisher = viewModel.editPublisher,
                        editIsbn = viewModel.editIsbn,
                        editIsdn = viewModel.editIsdn,
                        coverImagePath = coverImagePath,
                        isDownloading = isDownloading,
                        editSelectedAuthors = editSelectedAuthors,
                        allAuthors = allAuthors,
                        editSelectedBookshelf = editSelectedBookshelf,
                        allBookshelves = allBookshelves,
                        editSelectedCircle = editSelectedCircle,
                        allCircles = allCircles,
                        editSelectedSeries = editSelectedSeries,
                        allSeries = allSeries,
                        onTitleChange = viewModel::updateEditTitle,
                        onSubtitleChange = viewModel::updateEditSubtitle,
                        onReadingStatusChange = viewModel::updateEditReadingStatus,
                        onPublisherChange = viewModel::updateEditPublisher,
                        onIsbnChange = viewModel::updateEditIsbn,
                        onIsdnChange = viewModel::updateEditIsdn,
                        onPickFromGallery = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUrlSpecified = {
                            viewModel.downloadCoverImage(context, it)
                        },
                        onClearImage = viewModel::clearCoverImage,
                        onToggleAuthor = viewModel::toggleAuthor,
                        onRemoveAuthor = viewModel::removeAuthor,
                        onAddNewAuthor = viewModel::addNewAuthor,
                        onRenameAuthor = viewModel::renameAuthor,
                        onSetBookshelf = viewModel::setEditBookshelf,
                        onAddNewBookshelf = viewModel::addNewBookshelf,
                        onSetCircle = viewModel::setEditCircle,
                        onAddNewCircle = viewModel::addNewCircle,
                        onRenameCircle = viewModel::renameCircle,
                        onToggleSeries = viewModel::toggleSeries,
                        onRemoveSeries = viewModel::removeSeries,
                        onAddNewSeries = viewModel::addNewSeries,
                        onSave = viewModel::saveChanges,
                    )
                } else {
                    BookDetailSuccessScreen(
                        book = state.value,
                        bookshelf = viewModel.bookshelf,
                        navigateToAuthorDetail = navigateToAuthorDetail,
                        navigateToBookshelfDetail = navigateToBookshelfDetail,
                    )
                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            BookDetailTopBar(
                isEditing = false,
                onBackClick = {},
                onEditClick = {},
                onCancelEditClick = {},
                modifier = Modifier
                    .background(RebroColor.Background)
                    .fillMaxWidth()
            )
            BookDetailSuccessScreen(
                book = BookUiModel.Commercial(
                    id = 1L,
                    bookshelfId = 1L,
                    title = "吾輩は猫である",
                    subtitle = "上巻",
                    coverImageUrl = null,
                    readingStatus = ReadingStatus.READING,
                    authors = listOf("夏目漱石"),
                    seriesNames = emptyList(),
                    isbn = "978-4-00-310101-8",
                    publisher = "岩波書店",
                ),
                bookshelf = Bookshelf(id = 1L, name = "文学"),
                navigateToAuthorDetail = {},
                navigateToBookshelfDetail = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookDetailScreenDoujinPreview() {
    RebroTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
        ) {
            BookDetailTopBar(
                isEditing = false,
                onBackClick = {},
                onEditClick = {},
                onCancelEditClick = {},
                modifier = Modifier
                    .background(RebroColor.Background)
                    .fillMaxWidth()
            )
            BookDetailSuccessScreen(
                book = BookUiModel.Doujin(
                    id = 2L,
                    bookshelfId = 1L,
                    title = "とある同人誌",
                    subtitle = null,
                    coverImageUrl = null,
                    readingStatus = ReadingStatus.UNREAD,
                    authors = listOf("作家A", "作家B"),
                    seriesNames = emptyList(),
                    circleId = 1L,
                    circleName = "サークル名",
                    isdn = "ISDN-0000-0000",
                ),
                bookshelf = Bookshelf(id = 1L, name = "同人誌"),
                navigateToAuthorDetail = {},
                navigateToBookshelfDetail = {},
            )
        }
    }
}

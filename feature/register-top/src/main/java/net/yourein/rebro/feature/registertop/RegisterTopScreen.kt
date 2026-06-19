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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import net.yourein.rebro.model.entity.Circle
import org.koin.androidx.compose.koinViewModel

/**
 * 【デバッグ用】登録トップ画面。
 *
 * 検索トップの「最近登録した本」を確認するために、本を手早く DB へ登録するための臨時 UI。
 * register-top 本来の画面が実装され次第、丸ごと差し替える前提で作っている。
 */
@Composable
fun RegisterTopScreen(
    viewModel: RegisterTopViewModel = koinViewModel(),
) {
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val coverImagePath by viewModel.coverImagePath.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val selectedAuthors by viewModel.selectedAuthors.collectAsStateWithLifecycle()
    val allAuthors by viewModel.allAuthors.collectAsStateWithLifecycle()
    val selectedCircle by viewModel.selectedCircle.collectAsStateWithLifecycle()
    val allCircles by viewModel.allCircles.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.saveCoverImageFromPicker(it) }
    }

    RegisterTopScreen(
        lastResult = lastResult,
        coverImagePath = coverImagePath,
        isDownloading = isDownloading,
        selectedAuthors = selectedAuthors,
        allAuthors = allAuthors,
        selectedCircle = selectedCircle,
        allCircles = allCircles,
        onRegister = viewModel::registerBook,
        onRegisterRandom = viewModel::registerRandomBook,
        onPickFromGallery = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onUrlSpecified = viewModel::downloadCoverImage,
        onClearImage = viewModel::clearCoverImage,
        onToggleAuthor = viewModel::toggleAuthor,
        onRemoveAuthor = viewModel::removeAuthor,
        onAddNewAuthor = viewModel::addNewAuthor,
        onSetCircle = viewModel::setCircle,
        onAddNewCircle = viewModel::addNewCircle,
    )
}

@Composable
internal fun RegisterTopScreen(
    lastResult: String?,
    coverImagePath: String?,
    isDownloading: Boolean,
    selectedAuthors: List<Author>,
    allAuthors: List<Author>,
    selectedCircle: Circle?,
    allCircles: List<Circle>,
    onRegister: (title: String, subtitle: String, bookType: BookType, publisher: String) -> Unit,
    onRegisterRandom: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
    onSetCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var bookType by remember { mutableStateOf(BookType.COMMERCIAL) }
    var publisher by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .padding(16.dp),
    ) {
        Text(
            text = "Register A New Book",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

        HorizontalDivider()

        RegisterTopAutofillSection()

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
            label = { Text("タイトル（必須）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = { Text("サブタイトル") },
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

        when (bookType) {
            BookType.COMMERCIAL -> {
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("出版社") },
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
            onClick = {
                onRegister(title, subtitle, bookType, publisher)
                title = ""
                subtitle = ""
                publisher = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("この内容で登録")
        }

        OutlinedButton(
            onClick = onRegisterRandom,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ランダムなダミー本を登録")
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
            lastResult = "登録しました（bookId=3）：サンプル本 #3",
            coverImagePath = null,
            isDownloading = false,
            selectedAuthors = listOf(
                Author(id = 1, name = "テスト著者"),
            ),
            allAuthors = listOf(
                Author(id = 1, name = "テスト著者"),
                Author(id = 2, name = "別の著者"),
            ),
            selectedCircle = null,
            allCircles = emptyList(),
            onRegister = { _, _, _, _ -> },
            onRegisterRandom = {},
            onPickFromGallery = {},
            onUrlSpecified = {},
            onClearImage = {},
            onToggleAuthor = {},
            onRemoveAuthor = {},
            onAddNewAuthor = {},
            onSetCircle = {},
            onAddNewCircle = {},
        )
    }
}

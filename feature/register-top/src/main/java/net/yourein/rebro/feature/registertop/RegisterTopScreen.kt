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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { viewModel.saveCoverImageFromPicker(it) }
    }

    RegisterTopScreen(
        lastResult = lastResult,
        coverImagePath = coverImagePath,
        isDownloading = isDownloading,
        onRegister = viewModel::registerBook,
        onRegisterRandom = viewModel::registerRandomBook,
        onPickFromGallery = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onUrlSpecified = viewModel::downloadCoverImage,
        onClearImage = viewModel::clearCoverImage,
    )
}

@Composable
internal fun RegisterTopScreen(
    lastResult: String?,
    coverImagePath: String?,
    isDownloading: Boolean,
    onRegister: (title: String, subtitle: String, authorNames: String, bookType: BookType, detail: String) -> Unit,
    onRegisterRandom: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUrlSpecified: (String) -> Unit,
    onClearImage: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var authorNames by remember { mutableStateOf("") }
    var bookType by remember { mutableStateOf(BookType.COMMERCIAL) }
    var detail by remember { mutableStateOf("") }

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
        OutlinedTextField(
            value = authorNames,
            onValueChange = { authorNames = it },
            label = { Text("著者（「、」または「,」区切りで複数可）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )



        OutlinedTextField(
            value = detail,
            onValueChange = { detail = it },
            label = {
                Text(
                    when (bookType) {
                        BookType.COMMERCIAL -> "出版社"
                        BookType.DOUJIN -> "サークル名"
                    }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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
            onClick = {
                onRegister(title, subtitle, authorNames, bookType, detail)
                title = ""
                subtitle = ""
                authorNames = ""
                detail = ""
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
            onRegister = { _, _, _, _, _ -> },
            onRegisterRandom = {},
            onPickFromGallery = {},
            onUrlSpecified = {},
            onClearImage = {},
        )
    }
}

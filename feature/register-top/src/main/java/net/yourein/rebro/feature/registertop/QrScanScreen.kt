package net.yourein.rebro.feature.registertop

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.serialization.json.Json

private val lenientJson = Json { ignoreUnknownKeys = true }

private fun tryParseAutofillResult(bytes: ByteArray): AutofillResult? =
    runCatching {
        val jsonString = bytes.toString(Charsets.UTF_8)
        lenientJson.decodeFromString<AutofillResult>(jsonString)
    }.getOrNull()

@Composable
fun QrScanScreen(
    onApplyAutofill: (AutofillResult) -> Unit = {},
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PermissionChecker.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var parsedResult by remember { mutableStateOf<AutofillResult?>(null) }
    var lastScannedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var parseError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(16.dp),
    ) {
        Text(
            text = "QR Code Scanner",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

        if (hasCameraPermission) {
            QrCodeScannerView(
                onQrCodeDetected = { bytes ->
                    if (!bytes.contentEquals(lastScannedBytes)) {
                        lastScannedBytes = bytes
                        val result = tryParseAutofillResult(bytes)
                        parsedResult = result
                        parseError = result == null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(MaterialTheme.shapes.medium),
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "カメラの権限がないため、QRコードスキャナーを起動できません",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                )
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("カメラの権限を許可する")
                }
            }
        }

        parsedResult?.let { result ->
            HorizontalDivider()
            Text(
                text = "Rebro QR を検出しました",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Text(text = "タイトル: ${result.title}", fontSize = 13.sp)
            if (!result.circleName.isNullOrEmpty()) {
                Text(text = "サークル: ${result.circleName}", fontSize = 13.sp)
            }
            if (result.authorNames.isNotEmpty()) {
                Text(text = "著者: ${result.authorNames.joinToString()}", fontSize = 13.sp)
            }

            Button(
                onClick = { onApplyAutofill(result) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("この内容で入力する")
            }
        }

        if (parseError && parsedResult == null) {
            HorizontalDivider()
            Text(
                text = "Rebro QR として認識できないQRコードです",
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
            )
        }
    }
}

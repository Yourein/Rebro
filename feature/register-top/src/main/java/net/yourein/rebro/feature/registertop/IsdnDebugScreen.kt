package net.yourein.rebro.feature.registertop

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.yourein.rebro.model.isdn.IsdnItem
import net.yourein.rebro.model.isdn.IsdnResponse
import net.yourein.rebro.model.ndl.DcRecord
import net.yourein.rebro.model.ndl.SruResponse
import org.koin.androidx.compose.koinViewModel

@Composable
fun IsdnDebugScreen(
    onApplyAutofill: ((AutofillResult) -> Unit)? = null,
    viewModel: IsdnDebugViewModel = koinViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isdnResult by viewModel.isdnResult.collectAsStateWithLifecycle()
    val isbnResult by viewModel.isbnResult.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val barcodeAutofillResult by viewModel.barcodeAutofillResult.collectAsStateWithLifecycle()

    LaunchedEffect(barcodeAutofillResult) {
        val result = barcodeAutofillResult ?: return@LaunchedEffect
        viewModel.consumeBarcodeAutofillResult()
        onApplyAutofill?.invoke(result)
    }

    var codeInput by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(LookupMode.ISDN) }

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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .padding(16.dp),
    ) {
        Text(
            text = "Barcode Lookup",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

        // ── バーコードスキャナー ─────────────────
        if (hasCameraPermission) {
            BarcodeScannerView(
                onBarcodeDetected = { barcode ->
                    if (!isLoading) {
                        codeInput = barcode
                        when {
                            barcode.startsWith("278") -> mode = LookupMode.ISDN
                            barcode.startsWith("978") -> mode = LookupMode.ISBN
                        }
                        viewModel.fetchByBarcode(barcode)
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
                    text = "Allow this app to use camera for scanning a BarCode",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                )
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Allow Camera")
                }
            }
        }

        HorizontalDivider()

        // ── 手動入力 ─────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == LookupMode.ISDN,
                onClick = { mode = LookupMode.ISDN },
                label = { Text("ISDN") },
            )
            FilterChip(
                selected = mode == LookupMode.ISBN,
                onClick = { mode = LookupMode.ISBN },
                label = { Text("ISBN") },
            )
        }

        OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = {
                Text(
                    when (mode) {
                        LookupMode.ISDN -> "ISDN"
                        LookupMode.ISBN -> "ISBN"
                    }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { viewModel.fetch(codeInput.trim(), mode) },
            enabled = codeInput.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Fetch")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
            )
        }

        isdnResult?.let { response ->
            HorizontalDivider()
            IsdnResultSection(response)
        }

        isbnResult?.let { response ->
            HorizontalDivider()
            IsbnResultSection(response)
        }

        if (onApplyAutofill != null && (isdnResult != null || isbnResult != null)) {
            HorizontalDivider()
            Button(
                onClick = {
                    viewModel.buildAutofillResult()?.let { onApplyAutofill(it) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Apply to Form")
            }
        }
    }
}

@Composable
private fun IsbnResultSection(response: SruResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ResultRow("numberOfRecords", response.numberOfRecords)

        val records = response.records?.record
        if (records.isNullOrEmpty()) {
            Text("No Result")
            return
        }

        records.forEach { record ->
            record.recordData?.dc?.let { dc ->
                DcSection(dc)
            }
        }
    }
}

@Composable
private fun DcSection(dc: DcRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ResultRow("title", dc.title)
        ResultRow("creator", dc.creator)
        ResultRow("description", dc.description)
        ResultRow("publisher", dc.publisher)
        ResultRow("language", dc.language)
    }
}

@Composable
private fun IsdnResultSection(response: IsdnResponse) {
    val items = response.item
    if (items.isNullOrEmpty()) {
        Text("No Result")
        return
    }

    items.forEach { item ->
        ItemSection(item)
    }
}

@Composable
private fun ItemSection(item: IsdnItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ResultRow("key", item.key)
        ResultRow("disp-isdn", item.dispIsdn)
        ResultRow("region", item.region)
        ResultRow("class", item.clazz)
        ResultRow("type", item.type)
        ResultRow("rating_gender", item.ratingGender)
        ResultRow("rating_age", item.ratingAge)
        ResultRow("product-name", item.productName)
        ResultRow("product-yomi", item.productYomi)
        ResultRow("publisher-code", item.publisherCode)
        ResultRow("publisher-name", item.publisherName)
        ResultRow("publisher-yomi", item.publisherYomi)
        ResultRow("issue-date", item.issueDate)
        ResultRow("genre-code", item.genreCode)
        ResultRow("genre-name", item.genreName)
        ResultRow("genre-user", item.genreUser)
        ResultRow("c-code", item.cCode)
        ResultRow("author", item.author)
        ResultRow("shape", item.shape)
        ResultRow("contents", item.contents)
        ResultRow("price", item.price)
        ResultRow("price-unit", item.priceUnit)
        ResultRow("barcode2", item.barcode2)
        ResultRow("product-comment", item.productComment)
        ResultRow("product-style", item.productStyle)
        ResultRow("product-size", item.productSize)
        ResultRow("product-capacity", item.productCapacity)
        ResultRow("product-capacity-unit", item.productCapacityUnit)
        ResultRow("sample-image-uri", item.sampleImageUri)

        item.userOptions?.forEach { opt ->
            ResultRow("useroption", "${opt.property}: ${opt.value}")
        }
        item.externalLinks?.forEach { link ->
            ResultRow("external-link", "${link.title}: ${link.uri}")
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Text(
        text = "$label: $value",
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
}

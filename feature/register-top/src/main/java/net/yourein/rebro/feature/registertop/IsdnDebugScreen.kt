package net.yourein.rebro.feature.registertop

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.yourein.rebro.model.isdn.IsdnItem
import net.yourein.rebro.model.isdn.IsdnResponse
import net.yourein.rebro.model.ndl.DcRecord
import net.yourein.rebro.model.ndl.SruResponse
import org.koin.androidx.compose.koinViewModel

@Composable
fun IsdnDebugScreen(
    viewModel: IsdnDebugViewModel = koinViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isdnResult by viewModel.isdnResult.collectAsStateWithLifecycle()
    val isbnResult by viewModel.isbnResult.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var codeInput by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(LookupMode.ISDN) }

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
            text = "Barcode Debug",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
        )

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
                        LookupMode.ISDN -> "ISDN番号"
                        LookupMode.ISBN -> "ISBN番号"
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
    }
}

@Composable
private fun IsbnResultSection(response: SruResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ResultRow("numberOfRecords", response.numberOfRecords)

        val records = response.records?.record
        if (records.isNullOrEmpty()) {
            Text("結果なし")
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
        Text("結果なし")
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

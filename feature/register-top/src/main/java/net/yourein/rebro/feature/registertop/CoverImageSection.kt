package net.yourein.rebro.feature.registertop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.yourein.rebro.core.resources.DrawableR
import java.io.File

@Composable
internal fun RegisterTopCoverImageSection(
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
        Text(
            text = "Cover Image",
            fontSize = 16.sp,
        )

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

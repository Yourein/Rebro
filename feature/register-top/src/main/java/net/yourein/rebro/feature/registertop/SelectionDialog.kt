package net.yourein.rebro.feature.registertop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.yourein.rebro.model.entity.Author
import net.yourein.rebro.model.entity.Circle

// ── Author ───────────────────────────────────────

@Composable
internal fun AuthorSelectionSection(
    selectedAuthors: List<Author>,
    allAuthors: List<Author>,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "著者", fontSize = 16.sp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            selectedAuthors.forEach { author ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveAuthor(author) },
                    label = { Text(author.name) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("選択 / 追加")
        }
    }

    if (showDialog) {
        val selectedIds = selectedAuthors.map { it.id }.toSet()
        SelectionDialog(
            title = "著者を選択",
            items = allAuthors.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = true,
            addNewLabel = "新しい著者名",
            onItemToggled = { id ->
                allAuthors.find { it.id == id }?.let { onToggleAuthor(it) }
            },
            onNewItemAdded = onAddNewAuthor,
            onDismiss = { showDialog = false },
        )
    }
}

// ── Circle ───────────────────────────────────────

@Composable
internal fun CircleSelectionSection(
    selectedCircle: Circle?,
    allCircles: List<Circle>,
    onSelectCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "サークル", fontSize = 16.sp)

        if (selectedCircle != null) {
            InputChip(
                selected = true,
                onClick = { onSelectCircle(null) },
                label = { Text(selectedCircle.name) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (selectedCircle == null) "選択 / 追加" else "変更")
        }
    }

    if (showDialog) {
        val selectedIds = listOfNotNull(selectedCircle?.id).toSet()
        SelectionDialog(
            title = "サークルを選択",
            items = allCircles.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = false,
            addNewLabel = "新しいサークル名",
            onItemToggled = { id ->
                val circle = allCircles.find { it.id == id }
                onSelectCircle(if (selectedCircle?.id == id) null else circle)
            },
            onNewItemAdded = onAddNewCircle,
            onDismiss = { showDialog = false },
        )
    }
}

// ── Shared dialog ────────────────────────────────

@Composable
private fun SelectionDialog(
    title: String,
    items: List<Pair<Long, String>>,
    selectedIds: Set<Long>,
    multiSelect: Boolean,
    addNewLabel: String,
    onItemToggled: (Long) -> Unit,
    onNewItemAdded: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var newItemName by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("検索") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    if (filteredItems.isEmpty()) {
                        item {
                            Text(
                                text = "該当なし",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                    items(filteredItems, key = { it.first }) { (id, name) ->
                        val isSelected = id in selectedIds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemToggled(id) }
                                .padding(vertical = 4.dp),
                        ) {
                            if (multiSelect) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                            } else {
                                RadioButton(selected = isSelected, onClick = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text(addNewLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onNewItemAdded(newItemName)
                            newItemName = ""
                        },
                        enabled = newItemName.isNotBlank(),
                        modifier = Modifier.height(60.dp)
                    ) {
                        Text("追加")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完了")
            }
        },
    )
}

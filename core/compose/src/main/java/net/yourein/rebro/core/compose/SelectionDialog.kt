package net.yourein.rebro.core.compose

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import net.yourein.rebro.model.entity.Bookshelf
import net.yourein.rebro.model.entity.Circle
import net.yourein.rebro.model.entity.Series

// ── Author ───────────────────────────────────────

@Composable
fun AuthorSelectionSection(
    selectedAuthors: List<Author>,
    allAuthors: List<Author>,
    onToggleAuthor: (Author) -> Unit,
    onRemoveAuthor: (Author) -> Unit,
    onAddNewAuthor: (String) -> Unit,
    onRenameAuthor: (Long, String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "Author(s)", fontSize = 16.sp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            selectedAuthors.forEach { author ->
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text(author.name) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRemoveAuthor(author) },
                        )
                    },
                )
            }
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add author")
        }
    }

    if (showDialog) {
        val selectedIds = selectedAuthors.map { it.id }.toSet()
        SelectionDialog(
            title = "Select Author",
            items = allAuthors.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = true,
            addNewLabel = "New Author",
            onItemToggled = { id ->
                allAuthors.find { it.id == id }?.let { onToggleAuthor(it) }
            },
            onNewItemAdded = onAddNewAuthor,
            onItemRenamed = onRenameAuthor,
            onDismiss = { showDialog = false },
        )
    }
}

// ── Bookshelf ────────────────────────────────────

@Composable
fun BookshelfSelectionSection(
    selectedBookshelf: Bookshelf?,
    allBookshelves: List<Bookshelf>,
    onSelectBookshelf: (Bookshelf?) -> Unit,
    onAddNewBookshelf: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "Bookshelf", fontSize = 16.sp)

        if (selectedBookshelf != null) {
            InputChip(
                selected = true,
                onClick = {},
                label = { Text(selectedBookshelf.name) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSelectBookshelf(null) },
                    )
                },
            )
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (selectedBookshelf == null) "Select Bookshelf" else "Change Bookshelf")
        }
    }

    if (showDialog) {
        val selectedIds = listOfNotNull(selectedBookshelf?.id).toSet()
        SelectionDialog(
            title = "Select Bookshelf",
            items = allBookshelves.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = false,
            addNewLabel = "New Bookshelf",
            onItemToggled = { id ->
                val bookshelf = allBookshelves.find { it.id == id }
                onSelectBookshelf(if (selectedBookshelf?.id == id) null else bookshelf)
            },
            onNewItemAdded = onAddNewBookshelf,
            onDismiss = { showDialog = false },
        )
    }
}

// ── Circle ───────────────────────────────────────

@Composable
fun CircleSelectionSection(
    selectedCircle: Circle?,
    allCircles: List<Circle>,
    onSelectCircle: (Circle?) -> Unit,
    onAddNewCircle: (String) -> Unit,
    onRenameCircle: (Long, String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "Circle", fontSize = 16.sp)

        if (selectedCircle != null) {
            InputChip(
                selected = true,
                onClick = {},
                label = { Text(selectedCircle.name) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSelectCircle(null) },
                    )
                },
            )
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (selectedCircle == null) "Select circle" else "Change circle")
        }
    }

    if (showDialog) {
        val selectedIds = listOfNotNull(selectedCircle?.id).toSet()
        SelectionDialog(
            title = "Select circle",
            items = allCircles.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = false,
            addNewLabel = "New circle",
            onItemToggled = { id ->
                val circle = allCircles.find { it.id == id }
                onSelectCircle(if (selectedCircle?.id == id) null else circle)
            },
            onNewItemAdded = onAddNewCircle,
            onItemRenamed = onRenameCircle,
            onDismiss = { showDialog = false },
        )
    }
}

// ── Series ──────────────────────────────────────

@Composable
fun SeriesSelectionSection(
    selectedSeries: List<Series>,
    allSeries: List<Series>,
    onToggleSeries: (Series) -> Unit,
    onRemoveSeries: (Series) -> Unit,
    onAddNewSeries: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "Series", fontSize = 16.sp)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            selectedSeries.forEach { series ->
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text(series.name) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRemoveSeries(series) },
                        )
                    },
                )
            }
        }

        OutlinedButton(onClick = { showDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add series")
        }
    }

    if (showDialog) {
        val selectedIds = selectedSeries.map { it.id }.toSet()
        SelectionDialog(
            title = "Select Series",
            items = allSeries.map { it.id to it.name },
            selectedIds = selectedIds,
            multiSelect = true,
            addNewLabel = "New Series",
            onItemToggled = { id ->
                allSeries.find { it.id == id }?.let { onToggleSeries(it) }
            },
            onNewItemAdded = onAddNewSeries,
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
    onItemRenamed: ((Long, String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var newItemName by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var editingName by remember { mutableStateOf("") }

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
                    label = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    if (filteredItems.isEmpty()) {
                        item {
                            Text(
                                text = "Nothing found",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                    items(filteredItems, key = { it.first }) { (id, name) ->
                        val isSelected = id in selectedIds
                        val isEditing = editingId == id

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isEditing) onItemToggled(id)
                                }
                                .padding(vertical = 4.dp),
                        ) {
                            if (multiSelect) {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                            } else {
                                RadioButton(selected = isSelected, onClick = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            if (isEditing) {
                                OutlinedTextField(
                                    value = editingName,
                                    onValueChange = { editingName = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        val trimmed = editingName.trim()
                                        if (trimmed.isNotBlank() && trimmed != name) {
                                            onItemRenamed?.invoke(id, trimmed)
                                        }
                                        editingId = null
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { editingId = null },
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            } else {
                                Text(name, modifier = Modifier.weight(1f))
                                if (onItemRenamed != null) {
                                    IconButton(
                                        onClick = {
                                            editingId = id
                                            editingName = name
                                        },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
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
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}

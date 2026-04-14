package app.kotori.japanese.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kotori.japanese.LocalAppContainer
import kotlinx.coroutines.flow.Flow
import app.kotori.japanese.data.db.SavedItemEntity
import app.kotori.japanese.data.db.SavedSetEntity
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(
    onStudyVocab: (setKey: String) -> Unit = {},
    onItemClick: (type: String, id: String) -> Unit = { _, _ -> },
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var showHelp by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("saved")) {
            container.onboardingRepository.markScreenSeen("saved")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Saved",
            description = "Items you bookmark anywhere in the app appear here.\n\n" +
                "• ITEMS — All bookmarked entries grouped by type. Tap to open, tap the bookmark icon to remove.\n" +
                "• SETS — Organise items into named study sets. Create a set, then add bookmarked items to it.",
            onDismiss = { showHelp = false },
        )
    }

    val allItems by container.savedRepository.getAllItems()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val allSets by container.savedRepository.getAllSets()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = "Saved",
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Items", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Sets", modifier = Modifier.padding(vertical = 12.dp))
                }
            }

            when (selectedTab) {
                0 -> ItemsTab(
                    allItems = allItems,
                    onItemClick = onItemClick,
                    onStudyVocab = onStudyVocab,
                    onUnsave = { item ->
                        scope.launch {
                            container.savedRepository.toggle(
                                type = item.type,
                                itemId = item.itemId,
                                title = item.title,
                                reading = item.reading,
                                meaning = item.meaning,
                            )
                        }
                    },
                )
                1 -> SetsTab(
                    allSets = allSets,
                    allSavedItems = allItems,
                    onCreateSet = { name ->
                        scope.launch { container.savedRepository.createSet(name) }
                    },
                    onDeleteSet = { setId ->
                        scope.launch { container.savedRepository.deleteSet(setId) }
                    },
                    onAddItemToSet = { setId, savedItemId ->
                        scope.launch { container.savedRepository.addItemToSet(setId, savedItemId) }
                    },
                    onRemoveItemFromSet = { setId, savedItemId ->
                        scope.launch { container.savedRepository.removeItemFromSet(setId, savedItemId) }
                    },
                    getSetItems = { setId -> container.savedRepository.getItemsForSet(setId) },
                )
            }
        }
    }
}

// ── Items Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun ItemsTab(
    allItems: List<SavedItemEntity>,
    onItemClick: (type: String, id: String) -> Unit,
    onStudyVocab: (setKey: String) -> Unit,
    onUnsave: (SavedItemEntity) -> Unit,
) {
    if (allItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Bookmark entries to build your study sets.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val byType = allItems.groupBy { it.type }
    val typeOrder = listOf("vocab", "kanji", "grammar", "verb", "adjective", "noun", "phrase")
    val typeLabels = mapOf(
        "vocab"     to "Saved Vocabulary",
        "kanji"     to "Saved Kanji",
        "grammar"   to "Saved Grammar",
        "verb"      to "Saved Verbs",
        "adjective" to "Saved Adjectives",
        "noun"      to "Saved Nouns",
        "phrase"    to "Saved Phrases",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        typeOrder.forEach { type ->
            val items = byType[type] ?: return@forEach
            if (items.isEmpty()) return@forEach

            item {
                SectionHeader(
                    title = typeLabels[type] ?: type,
                    count = items.size,
                    onStudy = if (type == "vocab") ({ onStudyVocab("saved_vocab") }) else null,
                )
            }
            items(items, key = { it.id }) { item ->
                SavedItemRow(
                    item = item,
                    onClick = { onItemClick(item.type, item.itemId) },
                    onUnsave = { onUnsave(item) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, onStudy: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "$count item${if (count != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            if (onStudy != null) {
                Button(onClick = onStudy) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Study", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SavedItemRow(item: SavedItemEntity, onClick: () -> Unit, onUnsave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (item.reading.isNotBlank()) {
                Text(
                    text = item.reading,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            if (item.meaning.isNotBlank()) {
                Text(
                    text = item.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
        IconButton(onClick = onUnsave) {
            Icon(Icons.Default.Bookmark, contentDescription = "Unsave", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── Sets Tab ───────────────────────────────────────────────────────────────────

@Composable
private fun SetsTab(
    allSets: List<SavedSetEntity>,
    allSavedItems: List<SavedItemEntity>,
    onCreateSet: (name: String) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onAddItemToSet: (setId: Long, savedItemId: Long) -> Unit,
    onRemoveItemFromSet: (setId: Long, savedItemId: Long) -> Unit,
    getSetItems: (Long) -> Flow<List<SavedItemEntity>>,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newSetName by remember { mutableStateOf("") }
    var setToDelete by remember { mutableStateOf<SavedSetEntity?>(null) }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newSetName = "" },
            title = { Text("New Study Set") },
            text = {
                OutlinedTextField(
                    value = newSetName,
                    onValueChange = { newSetName = it },
                    label = { Text("Set name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSetName.isNotBlank()) {
                            onCreateSet(newSetName.trim())
                            newSetName = ""
                            showCreateDialog = false
                        }
                    },
                    enabled = newSetName.isNotBlank(),
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newSetName = "" }) { Text("Cancel") }
            },
        )
    }

    setToDelete?.let { set ->
        AlertDialog(
            onDismissRequest = { setToDelete = null },
            title = { Text("Delete set?") },
            text = { Text("\"${set.name}\" will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { onDeleteSet(set.id); setToDelete = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { setToDelete = null }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Create button
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Study Sets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Set")
            }
        }

        if (allSets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Create a set to organise your saved items into custom study groups.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allSets, key = { it.id }) { set ->
                SetCard(
                    set = set,
                    allSavedItems = allSavedItems,
                    onDelete = { setToDelete = set },
                    onAddItemToSet = { savedItemId -> onAddItemToSet(set.id, savedItemId) },
                    onRemoveItemFromSet = { savedItemId -> onRemoveItemFromSet(set.id, savedItemId) },
                    setItemsFlow = getSetItems(set.id),
                )
            }
        }
    }
}

@Composable
private fun SetCard(
    set: SavedSetEntity,
    allSavedItems: List<SavedItemEntity>,
    onDelete: () -> Unit,
    onAddItemToSet: (savedItemId: Long) -> Unit,
    onRemoveItemFromSet: (savedItemId: Long) -> Unit,
    setItemsFlow: Flow<List<SavedItemEntity>>,
) {
    val setItems by setItemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val setItemIds = remember(setItems) { setItems.map { it.id }.toSet() }
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add to \"${set.name}\"") },
            text = {
                if (allSavedItems.isEmpty()) {
                    Text("You haven't bookmarked any items yet. Bookmark entries from any detail screen first.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(allSavedItems, key = { it.id }) { item ->
                            val isInSet = item.id in setItemIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isInSet) onRemoveItemFromSet(item.id)
                                        else onAddItemToSet(item.id)
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = isInSet,
                                    onCheckedChange = {
                                        if (isInSet) onRemoveItemFromSet(item.id)
                                        else onAddItemToSet(item.id)
                                    },
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = buildString {
                                            append(item.type)
                                            if (item.reading.isNotBlank()) append(" · ${item.reading}")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Done") }
            },
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (setItems.isNotEmpty()) expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = set.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${setItems.size} item${if (setItems.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                if (setItems.isNotEmpty()) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add items",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete set",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Expanded items list
            if (expanded && setItems.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                setItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            if (item.reading.isNotBlank()) {
                                Text(
                                    text = item.reading,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                        }
                        IconButton(onClick = { onRemoveItemFromSet(item.id) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove from set",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

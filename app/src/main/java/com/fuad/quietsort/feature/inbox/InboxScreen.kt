package com.fuad.quietsort.feature.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fuad.quietsort.classify.InboxEngine
import com.fuad.quietsort.classify.SmartClassifier
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun InboxScreen(history: NotificationHistoryRepository) {
    val records by history.inbox.collectAsState(initial = emptyList())
    val rules by history.rules.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var appFilter by remember { mutableStateOf("All apps") }
    var categoryFilter by remember { mutableStateOf("All categories") }
    var dateFilter by remember { mutableStateOf("Any time") }
    var onlyUnread by remember { mutableStateOf(false) }
    var onlyFavorites by remember { mutableStateOf(false) }
    var showArchived by remember { mutableStateOf(false) }
    var groupDuplicates by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var message by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var appMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var dateMenu by remember { mutableStateOf(false) }

    val from = when (dateFilter) {
        "24 hours" -> System.currentTimeMillis() - 86_400_000L
        "7 days" -> System.currentTimeMillis() - 7L * 86_400_000L
        "30 days" -> System.currentTimeMillis() - 30L * 86_400_000L
        else -> null
    }
    val results = InboxEngine.organize(
        records, rules, search,
        app = appFilter.takeUnless { it == "All apps" },
        category = categoryFilter.takeUnless { it == "All categories" },
        onlyUnread = onlyUnread,
        onlyFavorites = onlyFavorites,
        showArchived = showArchived,
        fromMs = from,
        groupDuplicates = groupDuplicates
    )
    val visibleIds = results.map { it.notification.id }.toSet()

    fun execute(action: suspend (Set<Long>) -> Int) {
        val ids = selected.ifEmpty { visibleIds }
        scope.launch {
            try {
                message = "Modified " + action(ids) + " rows"
                selected = emptySet()
            } catch (_: Exception) { message = "Operation failed" }
        }
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Smart Inbox", style = MaterialTheme.typography.headlineMedium)
        Text(results.size.toString() + " groups from " + records.size + " saved notifications",
            style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(search, { search = it }, label = { Text("Search notifications") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box {
                OutlinedButton(onClick = { appMenu = true }) { Text(appFilter.take(18)) }
                DropdownMenu(appMenu, onDismissRequest = { appMenu = false }) {
                    (listOf("All apps") + records.map { it.packageName }.distinct().sorted()).forEach { value ->
                        DropdownMenuItem(text = { Text(value) }, onClick = { appFilter = value; appMenu = false })
                    }
                }
            }
            Box {
                OutlinedButton(onClick = { categoryMenu = true }) { Text(categoryFilter.take(18)) }
                DropdownMenu(categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    (listOf("All categories") + SmartClassifier.categories).forEach { value ->
                        DropdownMenuItem(text = { Text(value) }, onClick = { categoryFilter = value; categoryMenu = false })
                    }
                }
            }
        }
        Box {
            OutlinedButton(onClick = { dateMenu = true }) { Text(dateFilter) }
            DropdownMenu(dateMenu, onDismissRequest = { dateMenu = false }) {
                listOf("Any time", "24 hours", "7 days", "30 days").forEach { value ->
                    DropdownMenuItem(text = { Text(value) }, onClick = { dateFilter = value; dateMenu = false })
                }
            }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            FilterChip(onlyUnread, { onlyUnread = !onlyUnread }, label = { Text("Unread") })
            FilterChip(onlyFavorites, { onlyFavorites = !onlyFavorites }, label = { Text("Starred") })
            FilterChip(showArchived, { showArchived = !showArchived }, label = { Text("Archived") })
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            FilterChip(groupDuplicates, { groupDuplicates = !groupDuplicates },
                label = { Text("Group duplicates") })
            TextButton(onClick = { selected = visibleIds }) { Text("Select all") }
            TextButton(onClick = { selected = emptySet() }) { Text("Clear") }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick = { execute { history.setRead(it, true) } }) { Text("Read") }
            TextButton(onClick = { execute { history.setRead(it, false) } }) { Text("Unread") }
            TextButton(onClick = { execute { history.setFavorite(it, true) } }) { Text("Star") }
            TextButton(onClick = { execute { history.setFavorite(it, false) } }) { Text("Unstar") }
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(onClick = { execute { history.setArchived(it, true) } }) { Text("Archive") }
            TextButton(onClick = { execute { history.setArchived(it, false) } }) { Text("Restore") }
            TextButton(onClick = { confirmDelete = true }) { Text("Delete") }
            Text(selected.size.toString() + " selected", style = MaterialTheme.typography.bodySmall)
        }
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
        RuleEditor(history, rules)
        HorizontalDivider()
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(results, key = { it.notification.id }) { item ->
                val n = item.notification
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            selected = if (n.id in selected) selected - n.id else selected + n.id
                        }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row {
                            Checkbox(n.id in selected, onCheckedChange = { checked ->
                                selected = if (checked) selected + n.id else selected - n.id
                            })
                            Column {
                                Text(n.packageName, style = MaterialTheme.typography.labelMedium)
                                Text(item.category + " • " +
                                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(Date(n.postedAtMs)),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(n.title ?: "(No title)", style = MaterialTheme.typography.titleMedium)
                        Text(n.bigText ?: n.text ?: "(No preview)",
                            style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                        if (item.duplicateCount > 1) {
                            Text(item.duplicateCount.toString() + " similar items grouped",
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            listOfNotNull(
                                if (!n.isRead) "Unread" else null,
                                if (n.isFavorite) "Starred" else null,
                                if (n.isArchived) "Archived" else null
                            ).joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete notifications?") },
        text = { Text("Delete selected or filtered records and their events? This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = {
                confirmDelete = false
                execute { history.deleteNotifications(it) }
            }) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
    )
}

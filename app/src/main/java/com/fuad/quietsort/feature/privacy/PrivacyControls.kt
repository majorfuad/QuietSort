package com.fuad.quietsort.feature.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fuad.quietsort.data.preferences.PrivacyPreferences
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.launch

@Composable
fun PrivacyControls(
    preferences: PrivacyPreferences,
    history: NotificationHistoryRepository
) {
    val scope = rememberCoroutineScope()
    val exclusions by preferences.excludedApps.collectAsState(initial = emptySet())
    val observed by history.sourcePackages.collectAsState(initial = emptyList())
    val retentionDays by preferences.retentionPeriodDays.collectAsState(initial = 30)
    var packageInput by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    var daysExpanded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Text("Privacy and retention", style = MaterialTheme.typography.titleLarge)
        Text(
            "Excluded apps are never saved. Excluding an app also erases its existing captured history. " +
                "This does not stop the Android listener receiving its callbacks.",
            style = MaterialTheme.typography.bodySmall
        )

        val packages = (observed + exclusions).distinct().sorted()
        if (packages.isEmpty()) {
            Text("No captured app packages yet. You can enter a package name below.",
                style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Previously captured and excluded apps", style = MaterialTheme.typography.titleSmall)
            packages.forEach { pkg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pkg, modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = pkg in exclusions,
                        enabled = !busy,
                        onCheckedChange = { excluded ->
                            scope.launch {
                                busy = true
                                try {
                                    val erased = history.setAppExcluded(pkg, excluded)
                                    notice = if (excluded) "Excluded $pkg; erased $erased stored records."
                                    else "Allowed $pkg for future capture."
                                } catch (_: Exception) { notice = "Couldn't update app exclusion." }
                                finally { busy = false }
                            }
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = packageInput,
            onValueChange = { packageInput = it.trim() },
            label = { Text("Package ID (e.g., com.example.app)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            enabled = !busy && packageInput.isNotBlank(),
            onClick = {
                val packageName = packageInput.trim()
                scope.launch {
                    busy = true
                    try {
                        val erased = history.setAppExcluded(packageName, true)
                        notice = "Excluded $packageName; erased $erased records."
                        packageInput = ""
                    } catch (_: IllegalArgumentException) {
                        notice = "Enter a valid Android package ID."
                    } catch (_: Exception) {
                        notice = "Couldn't save the exclusion."
                    } finally { busy = false }
                }
            }
        ) { Text("Exclude app and erase its records") }

        Text("Automatically delete records older than", style = MaterialTheme.typography.titleSmall)
        Box {
            OutlinedButton(enabled = !busy, onClick = { daysExpanded = true }) {
                Text("$retentionDays days")
            }
            DropdownMenu(expanded = daysExpanded, onDismissRequest = { daysExpanded = false }) {
                listOf(1, 7, 30, 90).forEach { days ->
                    DropdownMenuItem(
                        text = { Text("$days days") },
                        onClick = {
                            daysExpanded = false
                            scope.launch {
                                busy = true
                                try {
                                    val count = history.setRetentionDays(days)
                                    notice = "Retention: $days days. Deleted $count old records."
                                } catch (_: Exception) { notice = "Couldn't update retention." }
                                finally { busy = false }
                            }
                        }
                    )
                }
            }
        }
        OutlinedButton(enabled = !busy, onClick = {
            scope.launch {
                busy = true
                try {
                    val erased = history.pruneExpired()
                    notice = "Removed $erased expired records."
                } catch (_: Exception) { notice = "Cleanup failed." }
                finally { busy = false }
            }
        }) { Text("Run cleanup now") }

        Button(
            enabled = !busy,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = { confirmClear = true }
        ) { Text("Erase all saved history") }
        Text(
            "Erasing history also turns capture off. It cannot be undone.",
            style = MaterialTheme.typography.bodySmall
        )
        if (notice.isNotBlank()) {
            Text(notice, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Erase notification history?") },
            text = { Text("All saved notifications and events will be deleted. Capture will turn off. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    scope.launch {
                        busy = true
                        try {
                            val erased = history.eraseAllHistory()
                            notice = "Erased $erased notification records. Capture disabled."
                        } catch (_: Exception) { notice = "History erasure failed." }
                        finally { busy = false }
                    }
                }) { Text("Erase everything") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

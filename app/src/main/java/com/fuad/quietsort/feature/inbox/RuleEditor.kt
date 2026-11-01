package com.fuad.quietsort.feature.inbox

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fuad.quietsort.classify.SmartClassifier
import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.launch

@Composable
fun RuleEditor(
    history: NotificationHistoryRepository,
    rules: List<ClassificationRuleEntity>
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf("KEYWORD") }
    var category by remember { mutableStateOf("Messages") }
    var typeMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }

    OutlinedButton(onClick = { expanded = !expanded }) {
        Text(if (expanded) "Hide rules" else "Rules (" + rules.size + ")")
    }
    if (expanded) {
        Text("Rules override the offline category classifier.",
            style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(name, { name = it }, label = { Text("Rule name") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pattern, { pattern = it }, label = { Text("App package or keyword") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                OutlinedButton(onClick = { typeMenu = true }) { Text(matchType) }
                DropdownMenu(typeMenu, onDismissRequest = { typeMenu = false }) {
                    listOf("KEYWORD", "TITLE", "APP").forEach { value ->
                        DropdownMenuItem(text = { Text(value) },
                            onClick = { matchType = value; typeMenu = false })
                    }
                }
            }
            Box {
                OutlinedButton(onClick = { categoryMenu = true }) { Text(category) }
                DropdownMenu(categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    SmartClassifier.categories.forEach { value ->
                        DropdownMenuItem(text = { Text(value) },
                            onClick = { category = value; categoryMenu = false })
                    }
                }
            }
        }
        Button(
            enabled = name.isNotBlank() && pattern.isNotBlank(),
            onClick = {
                scope.launch {
                    try {
                        history.addRule(name, matchType, pattern, category)
                        feedback = "Rule saved"
                        name = ""
                        pattern = ""
                    } catch (_: Exception) { feedback = "Could not save rule" }
                }
            }
        ) { Text("Save rule") }
        if (feedback.isNotBlank()) Text(feedback)
        rules.forEach { rule ->
            Row {
                Column(Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.labelLarge)
                    Text(rule.matchType + ": " + rule.pattern + " → " + rule.targetCategory,
                        style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = {
                    scope.launch {
                        try { history.deleteRule(rule.id) }
                        catch (_: Exception) { feedback = "Could not remove rule" }
                    }
                }) { Text("Remove") }
            }
        }
    }
}

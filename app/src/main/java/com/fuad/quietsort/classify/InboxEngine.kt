package com.fuad.quietsort.classify

import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity
import com.fuad.quietsort.data.local.entity.NotificationEntity
import java.util.Locale

data class InboxItem(
    val notification: NotificationEntity,
    val category: String,
    val duplicateCount: Int
)

object InboxEngine {
    fun organize(
        records: List<NotificationEntity>,
        rules: List<ClassificationRuleEntity>,
        search: String = "",
        app: String? = null,
        category: String? = null,
        onlyUnread: Boolean = false,
        onlyFavorites: Boolean = false,
        showArchived: Boolean = false,
        fromMs: Long? = null,
        toMs: Long? = null,
        groupDuplicates: Boolean = true
    ): List<InboxItem> {
        val query = search.trim()
        val ordered = records.asSequence()
            .filter { it.packageName != "com.fuad.quietsort.selftest" }
            .filter { (showArchived || !it.isArchived) && (!onlyUnread || !it.isRead) && (!onlyFavorites || it.isFavorite) }
            .filter { app == null || it.packageName == app }
            .filter { fromMs == null || it.postedAtMs >= fromMs }
            .filter { toMs == null || it.postedAtMs < toMs }
            .filter { query.isBlank() || listOfNotNull(it.title, it.text, it.bigText, it.packageName)
                .any { value -> value.contains(query, ignoreCase = true) } }
            .map { record ->
                InboxItem(
                    record,
                    SmartClassifier.classify(
                        record.packageName, record.title,
                        record.bigText ?: record.text,
                        record.category, rules
                    ),
                    1
                )
            }
            .filter { category == null || it.category == category }
            .sortedWith(compareByDescending<InboxItem> { it.notification.postedAtMs }.thenByDescending { it.notification.id })
            .toList()
        if (!groupDuplicates) return ordered
        val groups = linkedMapOf<String, MutableList<InboxItem>>()
        for (item in ordered) {
            val n = item.notification
            val key = listOf(
                n.packageName.lowercase(Locale.ROOT),
                (n.title ?: "").trim().lowercase(Locale.ROOT),
                (n.bigText ?: n.text ?: "").trim().lowercase(Locale.ROOT),
                item.category,
                n.postedAtMs / 3_600_000L
            ).joinToString("\u001f")
            groups.getOrPut(key) { mutableListOf() }.add(item)
        }
        return groups.values.map { members ->
            members.first().copy(duplicateCount = members.size)
        }
    }
}

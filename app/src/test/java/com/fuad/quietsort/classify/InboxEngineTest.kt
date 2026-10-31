package com.fuad.quietsort.classify

import com.fuad.quietsort.data.local.entity.NotificationEntity
import org.junit.Assert.*
import org.junit.Test

class InboxEngineTest {
    private fun item(id: Long, title: String, isRead: Boolean = false) =
        NotificationEntity(
            id = id, systemKey = "key" + id, lifecycleStartMs = id,
            packageName = "com.example.app", notificationId = id.toInt(),
            tag = null, postedAtMs = 1_000L + id, updatedAtMs = 1_000L + id,
            title = title, text = "Same body", bigText = null, isRead = isRead
        )

    @Test fun keywordFilterIsCaseInsensitive() {
        val result = InboxEngine.organize(
            listOf(item(1, "Invoice"), item(2, "Hello")), emptyList(), search = "INVOICE"
        )
        assertEquals(listOf(1L), result.map { it.notification.id })
    }

    @Test fun readFilterWorks() {
        val result = InboxEngine.organize(
            listOf(item(1, "A", true), item(2, "B")), emptyList(), onlyUnread = true
        )
        assertEquals(listOf(2L), result.map { it.notification.id })
    }

    @Test fun groupsExactDuplicatesButCanExpandAll() {
        val records = listOf(item(1, "Same"), item(2, "Same"))
        val grouped = InboxEngine.organize(records, emptyList())
        assertEquals(1, grouped.size)
        assertEquals(2, grouped.first().duplicateCount)
        assertEquals(2, InboxEngine.organize(records, emptyList(), groupDuplicates = false).size)
    }

    @Test fun excludesArchivedByDefault() {
        val archived = item(1, "A").copy(isArchived = true)
        assertTrue(InboxEngine.organize(listOf(archived), emptyList()).isEmpty())
        assertEquals(1, InboxEngine.organize(listOf(archived), emptyList(), showArchived = true).size)
    }
}

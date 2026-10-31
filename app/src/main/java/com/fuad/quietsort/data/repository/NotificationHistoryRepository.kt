package com.fuad.quietsort.data.repository

import androidx.room.withTransaction
import com.fuad.quietsort.capture.NormalizedNotification
import com.fuad.quietsort.data.local.QuietSortDatabase
import com.fuad.quietsort.data.local.entity.NotificationEntity
import com.fuad.quietsort.data.local.entity.NotificationEventEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serialized lifecycle processing. The Android notification key identifies an
 * active instance, while our row ID preserves an immutable historical identity.
 */
@Singleton
class NotificationHistoryRepository @Inject constructor(
    private val database: QuietSortDatabase
) {
    private val mutex = Mutex()
    private val dao get() = database.notificationDao()

    val capturedCount get() = dao.observeCapturedCount()
    val eventCount get() = dao.observeEventCount()

    suspend fun posted(value: NormalizedNotification, eventTimeMs: Long) = mutex.withLock {
        database.withTransaction {
            val active = dao.findActive(value.systemKey)
            if (active == null) {
                // Key reuse after a removal must create a fresh lifecycle.
                val lastStart = dao.latestLifecycleStart(value.systemKey) ?: -1L
                val start = maxOf(eventTimeMs, lastStart + 1L)
                val id = dao.insert(
                    NotificationEntity(
                        systemKey = value.systemKey,
                        lifecycleStartMs = start,
                        packageName = value.packageName,
                        notificationId = value.notificationId,
                        tag = value.tag,
                        title = value.title,
                        text = value.text,
                        bigText = value.bigText,
                        postedAtMs = value.postedAtMs,
                        updatedAtMs = eventTimeMs,
                        category = value.category,
                        channelId = value.channelId,
                        groupKey = value.groupKey,
                        isOngoing = value.isOngoing,
                        isClearable = value.isClearable,
                        isGroupSummary = value.isGroupSummary
                    )
                )
                dao.insertEvent(NotificationEventEntity(notificationId = id, type = "POSTED", occurredAtMs = eventTimeMs))
            } else {
                dao.update(
                    active.copy(
                        title = value.title,
                        text = value.text,
                        bigText = value.bigText,
                        updatedAtMs = eventTimeMs,
                        category = value.category,
                        channelId = value.channelId,
                        groupKey = value.groupKey,
                        isOngoing = value.isOngoing,
                        isClearable = value.isClearable,
                        isGroupSummary = value.isGroupSummary
                    )
                )
                dao.insertEvent(NotificationEventEntity(notificationId = active.id, type = "UPDATED", occurredAtMs = eventTimeMs))
            }
        }
    }

    suspend fun removed(systemKey: String, eventTimeMs: Long, reason: Int? = null) = mutex.withLock {
        database.withTransaction {
            val active = dao.findActive(systemKey) ?: return@withTransaction
            dao.update(active.copy(removedAtMs = eventTimeMs))
            dao.insertEvent(
                NotificationEventEntity(
                    notificationId = active.id,
                    type = "REMOVED",
                    occurredAtMs = eventTimeMs,
                    removalReason = reason
                )
            )
        }
    }
}

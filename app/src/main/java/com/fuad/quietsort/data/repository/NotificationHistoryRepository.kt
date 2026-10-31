package com.fuad.quietsort.data.repository

import androidx.room.withTransaction
import com.fuad.quietsort.capture.NormalizedNotification
import com.fuad.quietsort.data.local.QuietSortDatabase
import com.fuad.quietsort.data.preferences.PrivacyPreferences
import kotlinx.coroutines.flow.first
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
    private val database: QuietSortDatabase,
    private val privacy: PrivacyPreferences
) {
    private val mutex = Mutex()
    private val dao get() = database.notificationDao()

    val capturedCount get() = dao.observeCapturedCount()
    val eventCount get() = dao.observeEventCount()
    val sourcePackages get() = dao.observeSourcePackages()

    suspend fun posted(value: NormalizedNotification, eventTimeMs: Long) = mutex.withLock {
        if (!privacy.isCaptureEnabled.first() || value.packageName in privacy.excludedApps.first()) return@withLock
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
        if (!privacy.isCaptureEnabled.first()) return@withLock
        database.withTransaction {
            val active = dao.findActive(systemKey) ?: return@withTransaction
            if (active.packageName in privacy.excludedApps.first()) return@withTransaction
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
    /** Exclusion and erasure share the same mutex as capture operations. */
    suspend fun setAppExcluded(packageName: String, excluded: Boolean): Int = mutex.withLock {
        privacy.setExcludedPackage(packageName, excluded)
        if (excluded) database.withTransaction { dao.deletePackage(packageName) } else 0
    }

    suspend fun setRetentionDays(days: Int): Int = mutex.withLock {
        privacy.setRetentionDays(days)
        pruneLocked(System.currentTimeMillis())
    }

    suspend fun pruneExpired(nowMs: Long = System.currentTimeMillis()): Int = mutex.withLock {
        pruneLocked(nowMs)
    }

    private suspend fun pruneLocked(nowMs: Long): Int {
        val days = privacy.retentionPeriodDays.first()
        val cutoff = nowMs - days * 86_400_000L
        return database.withTransaction { dao.deleteOlderThan(cutoff) }
    }

    /** Stops capture and deletes all rows (including synthetic T06 records). */
    suspend fun eraseAllHistory(): Int = mutex.withLock {
        privacy.setCaptureEnabled(false)
        database.withTransaction { dao.deleteAll() }
    }

}

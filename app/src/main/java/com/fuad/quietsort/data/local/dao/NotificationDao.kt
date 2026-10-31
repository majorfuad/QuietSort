package com.fuad.quietsort.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fuad.quietsort.data.local.entity.NotificationEntity
import com.fuad.quietsort.data.local.entity.NotificationEventEntity
import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Update
    suspend fun update(notification: NotificationEntity)

    @Insert
    suspend fun insertEvent(event: NotificationEventEntity): Long

    @Query("SELECT * FROM notifications WHERE systemKey = :key AND removedAtMs IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun findActive(key: String): NotificationEntity?

    @Query("SELECT MAX(lifecycleStartMs) FROM notifications WHERE systemKey = :key")
    suspend fun latestLifecycleStart(key: String): Long?

    @Query("SELECT * FROM notifications ORDER BY postedAtMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Long

    @Query("SELECT COUNT(*) FROM notifications WHERE packageName != 'com.fuad.quietsort.selftest'")
    fun observeCapturedCount(): Flow<Int>

    @Query("SELECT DISTINCT packageName FROM notifications WHERE packageName != 'com.fuad.quietsort.selftest' ORDER BY packageName")
    fun observeSourcePackages(): Flow<List<String>>

    @Query("DELETE FROM notifications WHERE packageName = :packageName")
    suspend fun deletePackage(packageName: String): Int

    @Query("DELETE FROM notifications WHERE postedAtMs < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long): Int

    @Query("DELETE FROM notifications")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM notification_events")
    fun observeEventCount(): Flow<Int>
    @Query("SELECT * FROM notifications WHERE packageName != 'com.fuad.quietsort.selftest' ORDER BY postedAtMs DESC, id DESC LIMIT :limit")
    fun observeInbox(limit: Int = 1000): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM classification_rules ORDER BY priority, id")
    fun observeRules(): Flow<List<ClassificationRuleEntity>>

    @Insert
    suspend fun insertRule(rule: ClassificationRuleEntity): Long

    @Query("DELETE FROM classification_rules WHERE id = :id")
    suspend fun deleteRule(id: Long): Int

    @Query("UPDATE notifications SET isRead = :value WHERE id IN (:ids)")
    suspend fun setRead(ids: List<Long>, value: Boolean): Int

    @Query("UPDATE notifications SET isArchived = :value WHERE id IN (:ids)")
    suspend fun setArchived(ids: List<Long>, value: Boolean): Int

    @Query("UPDATE notifications SET isFavorite = :value WHERE id IN (:ids)")
    suspend fun setFavorite(ids: List<Long>, value: Boolean): Int

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    suspend fun deleteIds(ids: List<Long>): Int

}

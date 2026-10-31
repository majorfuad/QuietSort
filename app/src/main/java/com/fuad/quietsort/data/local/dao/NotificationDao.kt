package com.fuad.quietsort.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fuad.quietsort.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY postedAtMs DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Long
}

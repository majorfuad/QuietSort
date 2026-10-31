package com.fuad.quietsort.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["systemKey", "lifecycleStartMs"], unique = true),
        Index(value = ["packageName", "postedAtMs"]),
        Index(value = ["postedAtMs"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val systemKey: String,
    val lifecycleStartMs: Long,
    val packageName: String,
    val notificationId: Int,
    val tag: String?,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postedAtMs: Long,
    val updatedAtMs: Long,
    val removedAtMs: Long? = null,
    val category: String? = null,
    val channelId: String? = null,
    val groupKey: String? = null,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val isGroupSummary: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isRead: Boolean = false
)

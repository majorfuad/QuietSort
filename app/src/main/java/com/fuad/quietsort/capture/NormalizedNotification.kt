package com.fuad.quietsort.capture

data class NormalizedNotification(
    val systemKey: String,
    val packageName: String,
    val notificationId: Int,
    val tag: String?,
    val postedAtMs: Long,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val conversationTitle: String?,
    val category: String?,
    val channelId: String?,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val groupKey: String?,
    val isGroupSummary: Boolean
)

data class NotificationPayload(
    val key: String?,
    val packageName: String?,
    val id: Int,
    val tag: String?,
    val postTime: Long,
    val title: CharSequence?,
    val text: CharSequence?,
    val bigText: CharSequence?,
    val conversationTitle: CharSequence?,
    val category: String?,
    val channelId: String?,
    val ongoing: Boolean,
    val clearable: Boolean,
    val groupKey: String?,
    val groupSummary: Boolean
)

package com.fuad.quietsort.capture

import android.app.Notification
import android.service.notification.StatusBarNotification

object AndroidNotificationAdapter {
    fun extract(sbn: StatusBarNotification): NotificationPayload {
        val notification = sbn.notification
        val extras = notification.extras
        return NotificationPayload(
            key = sbn.key,
            packageName = sbn.packageName,
            id = sbn.id,
            tag = sbn.tag,
            postTime = sbn.postTime,
            title = extras?.getCharSequence(Notification.EXTRA_TITLE),
            text = extras?.getCharSequence(Notification.EXTRA_TEXT),
            bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT),
            conversationTitle = extras?.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE),
            category = notification.category,
            channelId = notification.channelId,
            ongoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0,
            clearable = sbn.isClearable,
            groupKey = sbn.groupKey,
            groupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        )
    }

    fun normalize(sbn: StatusBarNotification): NormalizedNotification? =
        NotificationNormalizer.normalize(extract(sbn))
}

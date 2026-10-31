package com.fuad.quietsort.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * T04: observe connection and callback activity without retaining notification content.
 * Processing/normalization is added in T05; encrypted persistence comes in T06/T07.
 */
class QuietNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        ListenerDiagnostics.connected()
    }

    override fun onListenerDisconnected() {
        ListenerDiagnostics.disconnected()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Deliberately do not inspect, log, queue or persist sbn.
        if (sbn != null) ListenerDiagnostics.posted()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn != null) ListenerDiagnostics.removed()
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        ListenerDiagnostics.rankingUpdated()
    }

    override fun onDestroy() {
        ListenerDiagnostics.disconnected()
        super.onDestroy()
    }
}

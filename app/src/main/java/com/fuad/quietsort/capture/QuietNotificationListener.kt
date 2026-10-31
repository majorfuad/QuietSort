package com.fuad.quietsort.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.fuad.quietsort.data.preferences.PrivacyPreferences
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Listener callback thread does no disk I/O.
 * Capture is explicitly off unless enabled in local preferences.
 * A bounded queue avoids unbounded retention of sensitive data in memory.
 */
@AndroidEntryPoint
class QuietNotificationListener : NotificationListenerService() {
    @Inject lateinit var preferences: PrivacyPreferences
    @Inject lateinit var history: NotificationHistoryRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val captureEnabled = AtomicBoolean(false)
    private val events = Channel<NotificationCaptureAction>(capacity = 256)

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            preferences.isCaptureEnabled.collect { enabled -> captureEnabled.set(enabled) }
        }
        scope.launch {
            for (event in events) {
                if (!captureEnabled.get()) continue
                // Fail closed, without logging any message content.
                try {
                    when (event) {
                        is NotificationCaptureAction.Posted ->
                            history.posted(event.value, event.timestampMs)
                        is NotificationCaptureAction.Removed ->
                            history.removed(event.key, event.timestampMs, event.reason)
                    }
                    ListenerDiagnostics.saved()
                } catch (_: Exception) {
                    ListenerDiagnostics.persistenceFailed()
                }
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ListenerDiagnostics.connected()
    }

    override fun onListenerDisconnected() {
        ListenerDiagnostics.disconnected()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        ListenerDiagnostics.posted()
        if (!captureEnabled.get()) return
        val value = try { AndroidNotificationAdapter.normalize(sbn) }
            catch (_: RuntimeException) { null }
        if (value != null && !events.trySend(NotificationCaptureAction.Posted(value, System.currentTimeMillis())).isSuccess) {
            ListenerDiagnostics.queueDropped()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        ListenerDiagnostics.removed()
        if (!captureEnabled.get()) return
        val key = sbn.key ?: return
        if (!events.trySend(NotificationCaptureAction.Removed(key, System.currentTimeMillis())).isSuccess) {
            ListenerDiagnostics.queueDropped()
        }
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        ListenerDiagnostics.rankingUpdated()
    }

    override fun onDestroy() {
        ListenerDiagnostics.disconnected()
        events.close()
        scope.cancel()
        super.onDestroy()
    }
}

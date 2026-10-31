package com.fuad.quietsort.capture

import android.service.notification.NotificationListenerService

/**
 * T03 registration-only service.
 * T04 adds lifecycle diagnostics; T06/T07 add encrypted persistence.
 * No notification content is processed or saved here.
 */
class QuietNotificationListener : NotificationListenerService()

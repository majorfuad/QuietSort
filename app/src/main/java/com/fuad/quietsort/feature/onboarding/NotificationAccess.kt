package com.fuad.quietsort.feature.onboarding

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.fuad.quietsort.capture.QuietNotificationListener

/** Checks this exact component, not merely whether any listener in our package is enabled. */
object NotificationAccess {
    fun isGranted(context: Context): Boolean {
        val expected = ComponentName(context, QuietNotificationListener::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return enabled.split(':').any { flattened ->
            ComponentName.unflattenFromString(flattened) == expected
        }
    }
}

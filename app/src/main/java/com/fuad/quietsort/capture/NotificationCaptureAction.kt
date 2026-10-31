package com.fuad.quietsort.capture

/**
 * Bounded, in-memory handoff between Android callbacks and a single DB worker.
 * Events carry sensitive data; never log these objects.
 */
sealed interface NotificationCaptureAction {
    data class Posted(val value: NormalizedNotification, val timestampMs: Long) : NotificationCaptureAction
    data class Removed(val key: String, val timestampMs: Long, val reason: Int? = null) : NotificationCaptureAction
}

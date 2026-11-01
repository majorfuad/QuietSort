package com.fuad.quietsort.capture

/** Capture health is diagnostic, not a guarantee that Android delivers every notification. */
enum class CaptureHealthStatus {
    ACCESS_REQUIRED, CAPTURE_PAUSED, DISCONNECTED, PROCESSING_ERRORS, HEALTHY
}

data class CaptureHealth(
    val status: CaptureHealthStatus,
    val headline: String,
    val detail: String
)

object CaptureHealthEvaluator {
    fun evaluate(
        hasAccess: Boolean,
        captureEnabled: Boolean,
        snapshot: ListenerSnapshot
    ): CaptureHealth = when {
        !hasAccess -> CaptureHealth(
            CaptureHealthStatus.ACCESS_REQUIRED,
            "Notification access required",
            "Enable QuietSort in Android notification access settings."
        )
        !captureEnabled -> CaptureHealth(
            CaptureHealthStatus.CAPTURE_PAUSED,
            "Capture paused",
            "Turn on Save notifications locally to begin recording new alerts."
        )
        !snapshot.connected -> CaptureHealth(
            CaptureHealthStatus.DISCONNECTED,
            "Listener not connected",
            "Android has not bound the listener. Recheck notification access and try reopening QuietSort."
        )
        snapshot.persistenceFailures > 0 || snapshot.droppedFromQueue > 0 -> CaptureHealth(
            CaptureHealthStatus.PROCESSING_ERRORS,
            "Capture needs attention",
            "Some events failed to save or were dropped in this process session."
        )
        else -> CaptureHealth(
            CaptureHealthStatus.HEALTHY,
            "Capture running",
            "The listener is connected. Android may still restrict some notifications."
        )
    }
}

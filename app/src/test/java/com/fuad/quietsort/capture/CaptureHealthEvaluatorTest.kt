package com.fuad.quietsort.capture

import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureHealthEvaluatorTest {
    @Test fun accessIsRequiredEvenWhenToggleIsOn() {
        val result = CaptureHealthEvaluator.evaluate(false, true, ListenerSnapshot(connected = true))
        assertEquals(CaptureHealthStatus.ACCESS_REQUIRED, result.status)
    }

    @Test fun pausedCaptureIsNotReportedAsUnhealthy() {
        val result = CaptureHealthEvaluator.evaluate(true, false, ListenerSnapshot())
        assertEquals(CaptureHealthStatus.CAPTURE_PAUSED, result.status)
    }

    @Test fun disconnectedListenerIsVisible() {
        val result = CaptureHealthEvaluator.evaluate(true, true, ListenerSnapshot())
        assertEquals(CaptureHealthStatus.DISCONNECTED, result.status)
    }

    @Test fun errorsOverrideConnectedState() {
        val result = CaptureHealthEvaluator.evaluate(
            true, true, ListenerSnapshot(connected = true, persistenceFailures = 1)
        )
        assertEquals(CaptureHealthStatus.PROCESSING_ERRORS, result.status)
    }

    @Test fun connectedWithoutErrorsIsHealthy() {
        val result = CaptureHealthEvaluator.evaluate(true, true, ListenerSnapshot(connected = true))
        assertEquals(CaptureHealthStatus.HEALTHY, result.status)
    }
}

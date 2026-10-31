package com.fuad.quietsort.data.retention

import org.junit.Assert.*
import org.junit.Test

class PrivacyPolicyTest {
    private val now = 100L * PrivacyPolicy.MILLIS_PER_DAY

    @Test fun captureIsOptIn() {
        assertFalse(PrivacyPolicy.shouldCapture(false, "com.example.app", emptySet(), now, now, 30))
    }

    @Test fun excludedAppsNeverPassPolicy() {
        assertFalse(PrivacyPolicy.shouldCapture(
            true, "com.example.app", setOf("com.example.app"), now, now, 30
        ))
    }

    @Test fun retentionRejectsOlderRecords() {
        assertFalse(PrivacyPolicy.shouldCapture(
            true, "com.example.app", emptySet(),
            now - 31 * PrivacyPolicy.MILLIS_PER_DAY, now, 30
        ))
    }

    @Test fun retentionAcceptsRecentRecords() {
        assertTrue(PrivacyPolicy.shouldCapture(
            true, "com.example.app", emptySet(),
            now - 2 * PrivacyPolicy.MILLIS_PER_DAY, now, 7
        ))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedRetentionRejected() {
        PrivacyPolicy.cutoff(now, 0)
    }
}

package com.fuad.quietsort.data.retention

/** Pure privacy rules, testable without Android framework dependencies. */
object PrivacyPolicy {
    const val MILLIS_PER_DAY = 86_400_000L

    fun cutoff(nowMs: Long, retentionDays: Int): Long {
        require(retentionDays in setOf(1, 7, 30, 90))
        return nowMs - retentionDays * MILLIS_PER_DAY
    }

    fun shouldCapture(
        enabled: Boolean,
        packageName: String,
        excludedPackages: Set<String>,
        postedAtMs: Long,
        nowMs: Long,
        retentionDays: Int
    ): Boolean = enabled &&
        packageName !in excludedPackages &&
        postedAtMs >= cutoff(nowMs, retentionDays)
}

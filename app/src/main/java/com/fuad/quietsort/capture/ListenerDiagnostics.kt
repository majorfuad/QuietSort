package com.fuad.quietsort.capture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ephemeral, process-local listener diagnostics.
 *
 * Intentionally stores no notification titles, bodies, package names, keys,
 * identities, or other content. Counters reset when the process restarts.
 */
data class ListenerSnapshot(
    val connected: Boolean = false,
    val lastConnectedAtMs: Long? = null,
    val lastDisconnectedAtMs: Long? = null,
    val postedEvents: Long = 0,
    val removedEvents: Long = 0,
    val rankingUpdates: Long = 0,
    val persistenceFailures: Long = 0,
    val droppedFromQueue: Long = 0
)

object ListenerDiagnostics {
    private val mutable = MutableStateFlow(ListenerSnapshot())
    val state: StateFlow<ListenerSnapshot> = mutable.asStateFlow()

    @Synchronized
    fun connected(now: Long = System.currentTimeMillis()) {
        mutable.value = mutable.value.copy(connected = true, lastConnectedAtMs = now)
    }

    @Synchronized
    fun disconnected(now: Long = System.currentTimeMillis()) {
        mutable.value = mutable.value.copy(connected = false, lastDisconnectedAtMs = now)
    }

    @Synchronized
    fun posted() {
        mutable.value = mutable.value.copy(postedEvents = mutable.value.postedEvents + 1)
    }

    @Synchronized
    fun removed() {
        mutable.value = mutable.value.copy(removedEvents = mutable.value.removedEvents + 1)
    }

    @Synchronized
    fun persistenceFailed() {
        mutable.value = mutable.value.copy(persistenceFailures = mutable.value.persistenceFailures + 1)
    }

    @Synchronized
    fun queueDropped() {
        mutable.value = mutable.value.copy(droppedFromQueue = mutable.value.droppedFromQueue + 1)
    }

    @Synchronized
    fun rankingUpdated() {
        mutable.value = mutable.value.copy(rankingUpdates = mutable.value.rankingUpdates + 1)
    }
}

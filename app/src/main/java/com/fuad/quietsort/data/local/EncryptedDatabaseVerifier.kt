package com.fuad.quietsort.data.local

import android.content.Context
import com.fuad.quietsort.data.local.entity.NotificationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Debug-only use from UI; writes only synthetic content and checks the disk header. */
class EncryptedDatabaseVerifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: QuietSortDatabase
) {
    suspend fun verify(): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val marker = "quietsort-self-test-$now"
        database.notificationDao().insert(
            NotificationEntity(
                systemKey = marker, lifecycleStartMs = now,
                packageName = "com.fuad.quietsort.selftest", notificationId = 0,
                tag = null, title = "Synthetic test", text = "No real notification data",
                bigText = null, postedAtMs = now, updatedAtMs = now
            )
        )
        val count = database.notificationDao().count()
        val file = context.getDatabasePath("quietsort.db")
        check(file.isFile) { "Database file not created" }
        val header = file.inputStream().use { stream ->
            ByteArray(16).also { bytes ->
                check(stream.read(bytes) == bytes.size) { "Database header unavailable" }
            }
        }
        check(!header.contentEquals("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII))) {
            "Plain SQLite header detected"
        }
        "Encrypted database opened. Synthetic records: $count. Plain SQLite header: absent."
    }
}

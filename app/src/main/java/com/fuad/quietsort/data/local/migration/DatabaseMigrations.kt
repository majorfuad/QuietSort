package com.fuad.quietsort.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS notification_events (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  notificationId INTEGER NOT NULL,
                  type TEXT NOT NULL,
                  occurredAtMs INTEGER NOT NULL,
                  removalReason INTEGER,
                  FOREIGN KEY(notificationId) REFERENCES notifications(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )""".trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_events_notificationId ON notification_events(notificationId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_events_occurredAtMs ON notification_events(occurredAtMs)")
        }
    }
}

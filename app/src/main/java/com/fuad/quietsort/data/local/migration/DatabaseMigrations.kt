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
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE notifications ADD COLUMN isRead INTEGER NOT NULL DEFAULT 0")
            db.execSQL("""CREATE TABLE IF NOT EXISTS classification_rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                matchType TEXT NOT NULL,
                pattern TEXT NOT NULL,
                targetCategory TEXT NOT NULL,
                priority INTEGER NOT NULL,
                enabled INTEGER NOT NULL
            )""".trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_classification_rules_priority ON classification_rules(priority)")
        }
    }
}


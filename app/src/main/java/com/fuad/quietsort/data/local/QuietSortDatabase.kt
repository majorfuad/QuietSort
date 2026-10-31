package com.fuad.quietsort.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fuad.quietsort.data.local.dao.NotificationDao
import com.fuad.quietsort.data.local.entity.NotificationEntity
import com.fuad.quietsort.data.local.entity.NotificationEventEntity
import com.fuad.quietsort.data.local.entity.ClassificationRuleEntity

@Database(
    entities = [NotificationEntity::class, NotificationEventEntity::class, ClassificationRuleEntity::class],
    version = 3,
    exportSchema = true
)
abstract class QuietSortDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

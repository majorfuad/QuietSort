package com.fuad.quietsort.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fuad.quietsort.data.local.dao.NotificationDao
import com.fuad.quietsort.data.local.entity.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1, exportSchema = true)
abstract class QuietSortDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

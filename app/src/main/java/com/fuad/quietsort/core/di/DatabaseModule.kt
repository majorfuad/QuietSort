package com.fuad.quietsort.core.di

import android.content.Context
import androidx.room.Room
import com.fuad.quietsort.core.security.DatabaseKeyManager
import com.fuad.quietsort.data.local.QuietSortDatabase
import com.fuad.quietsort.data.local.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DB_NAME = "quietsort.db"

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): QuietSortDatabase {
        System.loadLibrary("sqlcipher")
        val databaseFile = context.getDatabasePath(DB_NAME)
        val secret = DatabaseKeyManager(context).getOrCreatePassphrase(databaseFile.exists())
        // No fallback to framework SQLite. Failure to unlock must abort the operation.
        val factory = SupportOpenHelperFactory(secret)
        return Room.databaseBuilder(context, QuietSortDatabase::class.java, DB_NAME)
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun dao(database: QuietSortDatabase): NotificationDao = database.notificationDao()
}

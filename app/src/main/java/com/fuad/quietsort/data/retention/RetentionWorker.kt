package com.fuad.quietsort.data.retention

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fuad.quietsort.data.repository.NotificationHistoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RetentionWorkerDependencies {
    fun historyRepository(): NotificationHistoryRepository
}

/** Device-only maintenance. Never handles notification contents outside the encrypted Room DB. */
class RetentionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val deps = EntryPointAccessors.fromApplication(
            applicationContext,
            RetentionWorkerDependencies::class.java
        )
        deps.historyRepository().pruneExpired()
        Result.success()
    } catch (_: Exception) {
        Result.retry()
    }
}

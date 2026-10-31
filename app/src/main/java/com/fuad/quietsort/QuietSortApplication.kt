package com.fuad.quietsort

import android.app.Application
import com.fuad.quietsort.data.retention.RetentionScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuietSortApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetentionScheduler.schedule(this)
    }
}

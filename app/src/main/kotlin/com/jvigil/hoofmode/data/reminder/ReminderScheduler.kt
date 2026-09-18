package com.jvigil.hoofmode.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val WORK_NAME = "body_weight_reminder"

@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    fun schedule(frequencyDays: Int) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(frequencyDays.toLong(), TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

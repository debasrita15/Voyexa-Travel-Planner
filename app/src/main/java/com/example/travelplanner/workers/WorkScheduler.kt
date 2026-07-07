package com.example.travelplanner.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleLeaveReminder(
        context: Context,
        tripId: String,
        tripTitle: String,
        destination: String,
        dateRange: String,
        triggerAtEpochMs: Long
    ) {
        val delayMs = (triggerAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)

        val data = workDataOf(
            "tripId" to tripId,
            "tripTitle" to tripTitle,
            "destination" to destination,
            "dateRange" to dateRange
        )

        val req = OneTimeWorkRequestBuilder<LeaveReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("leave_reminder_$tripId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "leave_reminder_$tripId",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}

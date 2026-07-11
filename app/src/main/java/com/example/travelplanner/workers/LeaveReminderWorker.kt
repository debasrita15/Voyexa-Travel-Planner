package com.example.travelplanner.workers

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travelplanner.R
import com.example.travelplanner.util.Notifications

class LeaveReminderWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val tripTitle = inputData.getString("tripTitle").orEmpty()
        val destination = inputData.getString("destination").orEmpty()
        val dateRange = inputData.getString("dateRange").orEmpty()

        val notif = NotificationCompat.Builder(appContext, Notifications.CHANNEL_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Apply leave for $tripTitle")
            .setContentText("$destination • $dateRange")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Apply leave for $tripTitle\nDestination: $destination\nDates: $dateRange"
                )
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(appContext).notify((System.currentTimeMillis() % 100000).toInt(), notif)
        return Result.success()
    }
}

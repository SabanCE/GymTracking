package com.example.gymtracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        // Eğer bugün antrenman yapılmadıysa bildirim gönder
        if (!WorkoutPrefs.isWorkoutFinishedToday(context)) {
            sendNotification(context)
        }

        return Result.success()
    }

    private fun sendNotification(context: Context) {
        val channelId = "workout_reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Antrenman Hatırlatıcı", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Antrenman Vakti! 💪")
            .setContentText("Bugünkü antrenmanını henüz tamamlamadın. Hadi harekete geç!")
            .setSmallIcon(R.drawable.ic_workout) // Logonuzu buraya ekleyin
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
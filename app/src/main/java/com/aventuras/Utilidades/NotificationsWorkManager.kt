package com.aventuras.Utilidades

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aventuras.MainActivity
import com.aventuras.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationsWorkManager (appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Log.d("Miapp" , "Trabajo notificaciones ejecutado")

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        val docRef = firebaseDatabase.collection("NOTIFICACION").document("NOTI_DOC")
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val notificacion = documentSnapshot.getString("NOTI_TXT")
            val prefs = Prefs(applicationContext)
            val oldNotificacion = prefs.notificacion
            createNotification("Notificacion Aventuras" , "Programada 1 vez cada hora $notificacion")

            /*

            if (!oldNotificacion.equals(notificacion)) {
                // Si el String que hay en firebase es distinto del que
                // hemos guardado en las Shared Preferences lanzamos una notificación
                // y guardamos la nueva notificación en el shared preferences
                Log.d("Miapp" , "Hay una nueva historia en Firebase")
                prefs.notificacion = notificacion

                // Intent para abrir la app
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                // Mostramos la notificación
                var builder = NotificationCompat.Builder(applicationContext, "AVENTURAS_NOTIFY")
                    .setSmallIcon(R.drawable.ic_stat_ic_notification)
                    .setContentTitle(applicationContext.getString(R.string.app_name))
                    .setContentText(applicationContext.getString(R.string.nueva_av) + notificacion)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(applicationContext)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(1, builder.build())
                }

            }

            */

        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }


    fun createNotification(title: String, description: String) {

        var notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("101", "channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "101")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)

        notificationManager.notify(1, notificationBuilder.build())

    }
}

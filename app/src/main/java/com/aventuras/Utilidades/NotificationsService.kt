package com.aventuras.Utilidades

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.aventuras.MainActivity
import com.aventuras.R
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsService () : JobIntentService() {


    override fun onHandleWork(intent: Intent) {

        Log.d("Miapp" , "Notifications Service")

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        val docRef = firebaseDatabase.collection("NOTIFICACION").document("NOTI_DOC")
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val notificacion = documentSnapshot.getString("NOTI_TXT")
            val prefs = Prefs(applicationContext)
            val oldNotificacion = prefs.notificacion

            if (!oldNotificacion.equals(notificacion)) {
                // Si el String que hay en firebase es distinto del que
                // hemos guardado en las Shared Preferences lanzamos una notificación
                // y guardamos la nueva notificación en el shared preferences
                Log.d("Miapp" , "Hay una nueva historia en Firebase")

                CreateNotification(getString(R.string.app_name) , "Nueva aventura disponible!! $notificacion")
                prefs.notificacion = notificacion

            }


        }

    }


    fun CreateNotification(title: String, description: String) {

        // Intent para abrir la app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)


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
            .setContentIntent(pendingIntent)

        notificationManager.notify(1, notificationBuilder.build())

    }



}
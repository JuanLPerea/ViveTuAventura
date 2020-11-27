package com.aventuras.Utilidades

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*


class AlarmReceiver () : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("Miapp" , intent!!.action.toString())

        if (intent!!.action == "android.intent.action.BOOT_COMPLETED") {
            // Set the alarm here.

            // Programar la alarma
            var time = Calendar.getInstance()

            time.set(Calendar.HOUR_OF_DAY, 16)
            time.set(Calendar.MINUTE, 0)
            time.set(Calendar.SECOND, 0)

            var am = context!!.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val i = Intent (context!!, AlarmReceiver::class.java)
            i.setAction("android.intent.action.NOTIFY")
            val pi =  PendingIntent.getBroadcast(context!!, 0, i, PendingIntent.FLAG_ONE_SHOT)
            am?.setRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis , 1000 * 60 * 10 , pi)

            if (Build.VERSION.SDK_INT >= 23){
                am!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time.getTimeInMillis(),pi);
            }
            else{
                am!!.set(AlarmManager.RTC_WAKEUP,time.getTimeInMillis(),pi);
            }
        }


        val service = Intent(context, NotificationsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.startForegroundService(service)
        } else {
            context!!.startService(service)
        }


    }


}
package com.cloudanalytica.repeattask.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.text.format.DateUtils
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cloudanalytica.repeattask.MainActivity.Companion.SERVICE_STARTED_CALLBACK


class AlarmService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("AlarmService", "Started")
        val broadCastIntent = Intent(SERVICE_STARTED_CALLBACK)
        broadCastIntent.putExtra(
            "message", "${
                DateUtils.formatDateTime(this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or
                        DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY)
            } Alarm Triggered\n"
        )
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

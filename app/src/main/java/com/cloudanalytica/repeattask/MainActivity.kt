package com.cloudanalytica.repeattask

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.cloudanalytica.alarm.R
import com.cloudanalytica.alarm.databinding.ActivityMainBinding
import com.cloudanalytica.repeattask.service.AlarmService
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mainViewBinding: ActivityMainBinding
    private var minutes = 0
    private var hours = 0

    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
    var triggerTimeStamp = 0L
    private var diffTime = 0L

    companion object {
        val ALARMRECEIVERREQUESTCODE = 25
        var SERVICE_STARTED_CALLBACK = "service_started"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewBinding = ActivityMainBinding.inflate(layoutInflater)
        val mView = mainViewBinding.root
        setContentView(mView)

        mainViewBinding.btnTimer.setOnClickListener(this)
        mainViewBinding.btnStart.setOnClickListener(this)
        mainViewBinding.btnStop.setOnClickListener(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter(SERVICE_STARTED_CALLBACK)
        )

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.btnStart -> {
                if (triggerTimeStamp != 0L) {
                    startAlarmManager(triggerTimeStamp)
                    mainViewBinding.tvLogs.text =
                        mainViewBinding.tvLogs.text.toString() + "${
                            DateUtils.formatDateTime(
                                this, System.currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or
                                        DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY
                            )
                        } Alarm was setup\n"
                } else {
                    Toast.makeText(this, "Alarm Duration is 0", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.btnStop -> {
                alarmMgr?.let {
                    it.cancel(alarmIntent)
                    mainViewBinding.tvLogs.text =
                        mainViewBinding.tvLogs.text.toString() + "${
                            DateUtils.formatDateTime(
                                this,
                                System.currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or
                                        DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY
                            )
                        } Alarm was destroyed\n"
                }
            }

            R.id.btnTimer -> {
                showTimerDialog()
            }

        }

    }


    private fun showTimerDialog() {

        val mCalendar = Calendar.getInstance()

        val timerDialog = TimePickerDialog(
            this,
            { view, hourOfDay, minute ->
                minutes = minute
                hours = hourOfDay

                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                mCalendar.set(Calendar.MINUTE, minute)
                Log.e("TimeStamp", "${mCalendar.timeInMillis}")
                calculateMinutes(mCalendar.timeInMillis)
                triggerTimeStamp = mCalendar.timeInMillis
            },
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            false
        )
        timerDialog.show()
        timerDialog.setTitle("Choose Time")
    }

    private fun calculateMinutes(selectedTime: Long) {

        diffTime = selectedTime - System.currentTimeMillis()
        val diffMinutes = (diffTime / 1000) / 60
        val diffSeconds = (diffTime / 1000) % 60

        mainViewBinding.tvTime.text =
            "Alarm will Trigger in  $diffMinutes Minutes(s) and $diffSeconds Second(s)"

    }

    private fun startAlarmManager(triggerTime: Long) {

        alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, AlarmService::class.java).let { intent ->
            PendingIntent.getService(this, ALARMRECEIVERREQUESTCODE, intent, 0)
        }

        Log.e("Repeat", "${mainViewBinding.checkRepeat.isChecked}")
        Log.e("AlarmTriggerAt", "$triggerTime")
        Log.e("AlarmRepeatAt", "$diffTime")



        if (!mainViewBinding.checkRepeat.isChecked) {
            alarmMgr?.set(
                AlarmManager.RTC,
                triggerTime,
                alarmIntent
            )
        } else {
            alarmMgr?.setRepeating(
                AlarmManager.RTC,
                triggerTime,
                diffTime,
                alarmIntent
            )
        }
        triggerTimeStamp = 0L

    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            mainViewBinding.tvLogs.text = mainViewBinding.tvLogs.text.toString() + message
        }
    }


}
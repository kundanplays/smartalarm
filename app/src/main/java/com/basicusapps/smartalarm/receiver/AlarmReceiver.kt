package com.basicusapps.smartalarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.basicusapps.smartalarm.ui.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId != -1L) {
            val alarmIntent = AlarmActivity.newIntent(context, alarmId)
            context.startActivity(alarmIntent)
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
} 
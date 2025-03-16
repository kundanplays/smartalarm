package com.basicusapps.smartalarm.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.basicusapps.smartalarm.data.model.Alarm
import com.basicusapps.smartalarm.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        val triggerTime = calculateNextTriggerTime(alarm)
        val pendingIntent = createPendingIntent(alarm.id)

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
            pendingIntent
        )
    }

    fun cancel(alarmId: Long) {
        val pendingIntent = createPendingIntent(alarmId)
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        // TODO: Implement proper next trigger time calculation based on repeat days
        val now = LocalDateTime.now()
        val triggerTime = LocalDateTime.of(
            now.toLocalDate(),
            alarm.time
        )

        return if (triggerTime.isBefore(now)) {
            triggerTime.plusDays(1)
        } else {
            triggerTime
        }.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun createPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }

        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
} 
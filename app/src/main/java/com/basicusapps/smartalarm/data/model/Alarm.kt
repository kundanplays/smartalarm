package com.basicusapps.smartalarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.basicusapps.smartalarm.data.db.Converters
import java.time.DayOfWeek
import com.basicusapps.smartalarm.domain.model.MissionType
import com.basicusapps.smartalarm.domain.model.MissionDifficulty
import java.time.LocalTime

@Entity(tableName = "alarms")
@TypeConverters(Converters::class)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val missionType: MissionType = MissionType.NONE,
    val missionDifficulty: MissionDifficulty = MissionDifficulty.EASY,
    val soundUri: String = "",
    val volume: Int = 100,
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 5, // in minutes
    val snoozeCount: Int = 3
) {
    fun getTime(): LocalTime = LocalTime.of(hour, minute)
} 
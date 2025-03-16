package com.basicusapps.smartalarm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.basicusapps.smartalarm.data.db.Converters
import java.time.LocalDateTime
import com.basicusapps.smartalarm.domain.model.MissionType

@Entity(tableName = "alarm_statistics")
@TypeConverters(Converters::class)
data class AlarmStatistics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val completionTime: LocalDateTime,
    val missionType: MissionType,
    val attemptsUsed: Int,
    val isSuccessful: Boolean = true
)
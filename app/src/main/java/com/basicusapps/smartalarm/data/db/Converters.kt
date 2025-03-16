package com.basicusapps.smartalarm.data.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.basicusapps.smartalarm.domain.model.MissionType
import com.basicusapps.smartalarm.domain.model.MissionDifficulty
import java.time.DayOfWeek

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromMissionType(value: MissionType): String {
        return value.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }

    @TypeConverter
    fun fromMissionDifficulty(value: MissionDifficulty): String {
        return value.name
    }

    @TypeConverter
    fun toMissionDifficulty(value: String): MissionDifficulty {
        return MissionDifficulty.valueOf(value)
    }

    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>): String {
        return days.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDayOfWeekSet(value: String): Set<DayOfWeek> {
        if (value.isEmpty()) return emptySet()
        return value.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }
} 
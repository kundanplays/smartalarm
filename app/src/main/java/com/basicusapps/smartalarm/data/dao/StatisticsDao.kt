package com.basicusapps.smartalarm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.basicusapps.smartalarm.data.model.AlarmStatistics
import com.basicusapps.smartalarm.data.model.MissionType
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM alarm_statistics ORDER BY completionTime DESC")
    fun getAllStatistics(): Flow<List<AlarmStatistics>>

    @Query("SELECT * FROM alarm_statistics WHERE alarmId = :alarmId ORDER BY completionTime DESC")
    fun getStatisticsByAlarmId(alarmId: Long): Flow<List<AlarmStatistics>>

    @Insert
    suspend fun insert(statistics: AlarmStatistics)

    @Query("SELECT COUNT(*) FROM alarm_statistics")
    suspend fun getTotalAlarms(): Int

    @Query("SELECT COUNT(*) FROM alarm_statistics WHERE isSuccessful = 1")
    suspend fun getSuccessfulAlarms(): Int

    @Query("SELECT AVG(attemptsUsed) FROM alarm_statistics WHERE isSuccessful = 1")
    suspend fun getAverageCompletionTime(): Double?

    @Query("""
        SELECT missionType FROM alarm_statistics 
        GROUP BY missionType 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedMissionType(): MissionType?

    @Query("""
        SELECT AVG(CASE WHEN isSuccessful = 1 THEN 1.0 ELSE 0.0 END) 
        FROM alarm_statistics 
        WHERE missionType = :missionType
    """)
    suspend fun getSuccessRateForMission(missionType: MissionType): Double?
} 
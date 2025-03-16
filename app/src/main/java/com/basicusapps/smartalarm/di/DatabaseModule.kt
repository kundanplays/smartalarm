package com.basicusapps.smartalarm.di

import android.content.Context
import com.basicusapps.smartalarm.data.dao.AlarmDao
import com.basicusapps.smartalarm.data.dao.StatisticsDao
import com.basicusapps.smartalarm.data.db.AlarmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlarmDatabase {
        return AlarmDatabase.getDatabase(context)
    }

    @Provides
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    fun provideStatisticsDao(database: AlarmDatabase): StatisticsDao {
        return database.statisticsDao()
    }
} 
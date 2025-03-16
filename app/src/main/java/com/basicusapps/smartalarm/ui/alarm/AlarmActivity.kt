package com.basicusapps.smartalarm.ui.alarm

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.basicusapps.smartalarm.data.model.MissionType
import com.basicusapps.smartalarm.databinding.ActivityAlarmBinding
import com.basicusapps.smartalarm.ui.missions.MissionLauncher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private val viewModel: AlarmViewModel by viewModels()

    @Inject
    lateinit var missionLauncher: MissionLauncher

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) {
            finish()
            return
        }

        acquireWakeLock()
        observeViewModel()
        viewModel.loadAlarm(alarmId)

        binding.dismissButton.setOnClickListener {
            viewModel.getCurrentAlarm()?.let { alarm ->
                if (alarm.missionType == MissionType.NONE) {
                    viewModel.onAlarmDismissed(alarm.id)
                    stopAlarmAndFinish()
                } else {
                    missionLauncher.launchMission(this, alarm)
                }
            }
        }

        binding.snoozeButton.setOnClickListener {
            viewModel.getCurrentAlarm()?.let { alarm ->
                if (alarm.allowSnooze) {
                    viewModel.onAlarmSnoozed(alarm.id)
                    stopAlarmAndFinish()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alarm.collect { alarm ->
                    alarm?.let {
                        binding.alarmLabel.text = it.label
                        binding.snoozeButton.isEnabled = it.allowSnooze

                        if (mediaPlayer == null) {
                            startAlarm(it.soundResId, it.vibrate)
                        }
                    }
                }
            }
        }
    }

    private fun startAlarm(soundResId: Int, shouldVibrate: Boolean) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmActivity, soundResId)
            isLooping = true
            prepare()
            start()
        }

        if (shouldVibrate) {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 500, 500)
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    pattern,
                    0
                )
            )
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "SmartAlarm:AlarmLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
    }

    private fun stopAlarmAndFinish() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        wakeLock?.release()
        wakeLock = null

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmAndFinish()
    }

    companion object {
        private const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun newIntent(context: Context, alarmId: Long): Intent {
            return Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
        }
    }
} 
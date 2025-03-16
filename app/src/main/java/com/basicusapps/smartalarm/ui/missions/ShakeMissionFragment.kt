package com.basicusapps.smartalarm.ui.missions

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentShakeMissionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShakeMissionFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentShakeMissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShakeMissionViewModel by viewModels()
    private val args: ShakeMissionFragmentArgs by navArgs()

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShakeMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSensor()
        observeViewModel()
        viewModel.startMission(args.difficulty)
    }

    private fun setupSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shakeState.collect { state ->
                    updateUI(state)
                    if (state.isComplete) {
                        handleMissionComplete()
                    }
                }
            }
        }
    }

    private fun updateUI(state: ShakeState) {
        binding.apply {
            shakeProgress.progress = state.progress
            progressText.text = getString(R.string.shake_progress_format, state.progress)
            timeElapsedText.text = getString(R.string.shake_time_elapsed, state.elapsedSeconds)
        }
    }

    private fun handleMissionComplete() {
        viewModel.onMissionCompleted(args.alarmId)
        findNavController().popBackStack()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            viewModel.onSensorChanged(
                event.values[0],
                event.values[1],
                event.values[2]
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
}
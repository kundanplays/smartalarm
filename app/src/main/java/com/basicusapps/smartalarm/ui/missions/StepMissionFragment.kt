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
import com.basicusapps.smartalarm.databinding.FragmentStepMissionBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StepMissionFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStepMissionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StepMissionViewModel by viewModels()
    private val args: StepMissionFragmentArgs by navArgs()

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStepSensor()
        observeViewModel()
        viewModel.startMission(args.difficulty)
    }

    private fun setupStepSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Snackbar.make(
                binding.root,
                R.string.step_sensor_not_available,
                Snackbar.LENGTH_INDEFINITE
            ).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stepState.collect { state ->
                    updateUI(state)
                    if (state.isComplete) {
                        handleMissionComplete()
                    }
                }
            }
        }
    }

    private fun updateUI(state: StepState) {
        binding.apply {
            stepProgress.progress = state.progress
            stepCountText.text = getString(R.string.step_count_format, state.currentSteps, state.targetSteps)
            targetStepsText.text = getString(R.string.step_target_format, state.targetSteps)
            timeElapsedText.text = getString(R.string.step_time_elapsed, state.elapsedSeconds)
        }
    }

    private fun handleMissionComplete() {
        viewModel.onMissionCompleted(args.alarmId)
        findNavController().popBackStack()
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
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
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            if (viewModel.stepState.value.currentSteps == 0) {
                viewModel.setInitialSteps(steps)
            }
            viewModel.onStepDetected(steps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
} 
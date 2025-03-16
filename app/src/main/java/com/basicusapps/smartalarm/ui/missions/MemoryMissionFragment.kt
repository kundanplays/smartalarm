package com.basicusapps.smartalarm.ui.missions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentMemoryMissionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MemoryMissionFragment : Fragment(), MemoryCardAdapter.OnCardClickListener {

    private var _binding: FragmentMemoryMissionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MemoryMissionViewModel by viewModels()
    private val args: MemoryMissionFragmentArgs by navArgs()
    private lateinit var adapter: MemoryCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoryMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.startGame(args.difficulty)
    }

    private fun setupRecyclerView() {
        adapter = MemoryCardAdapter(this)
        binding.memoryGrid.apply {
            layoutManager = GridLayoutManager(context, getGridSize())
            adapter = this@MemoryMissionFragment.adapter
        }
    }

    private fun getGridSize() = when (args.difficulty) {
        MissionDifficulty.EASY -> 4
        MissionDifficulty.MEDIUM -> 5
        MissionDifficulty.HARD -> 6
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.gameState.collect { state ->
                updateUI(state)
                if (state.isGameComplete) {
                    handleGameCompletion()
                }
            }
        }
    }

    private fun updateUI(state: MemoryGameState) {
        adapter.submitList(state.cards)
        binding.pairsFoundText.text = getString(
            R.string.pairs_found,
            state.pairsFound,
            state.totalPairs
        )
        binding.timeElapsedText.text = getString(
            R.string.time_elapsed,
            state.elapsedSeconds
        )
    }

    override fun onCardClick(position: Int) {
        viewModel.onCardClicked(position)
    }

    private fun flipCard(view: View, isFaceUp: Boolean) {
        val rotation = if (isFaceUp) 180f else 0f
        val anim = ObjectAnimator.ofFloat(view, "rotationY", rotation)
        anim.duration = 300
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }

    private fun handleGameCompletion() {
        lifecycleScope.launch {
            delay(500) // Short delay for the last pair animation
            viewModel.onMissionCompleted(args.alarmId)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
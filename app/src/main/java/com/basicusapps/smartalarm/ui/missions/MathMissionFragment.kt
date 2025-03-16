package com.basicusapps.smartalarm.ui.missions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.basicusapps.smartalarm.R
import com.basicusapps.smartalarm.databinding.FragmentMathMissionBinding
import com.basicusapps.smartalarm.data.model.MissionDifficulty
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class MathMissionFragment : Fragment() {

    private var _binding: FragmentMathMissionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MathMissionViewModel by viewModels()
    private val args: MathMissionFragmentArgs by navArgs()

    private var correctAnswer = 0
    private var attemptsLeft = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMathMissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        generateMathProblem(args.difficulty)
        setupUI()
    }

    private fun setupUI() {
        binding.submitButton.setOnClickListener {
            checkAnswer()
        }
        updateAttemptsText()
    }

    private fun generateMathProblem(difficulty: MissionDifficulty) {
        val (num1, num2, operator) = when (difficulty) {
            MissionDifficulty.EASY -> generateEasyProblem()
            MissionDifficulty.MEDIUM -> generateMediumProblem()
            MissionDifficulty.HARD -> generateHardProblem()
        }

        correctAnswer = calculateAnswer(num1, num2, operator)
        val problem = formatProblem(num1, num2, operator)
        binding.problemText.text = problem
    }

    private fun generateEasyProblem(): Triple<Int, Int, String> {
        val num1 = Random.nextInt(1, 21)
        val num2 = Random.nextInt(1, 21)
        return Triple(num1, num2, if (Random.nextBoolean()) "+" else "-")
    }

    private fun generateMediumProblem(): Triple<Int, Int, String> {
        val num1 = Random.nextInt(10, 51)
        val num2 = Random.nextInt(10, 51)
        val operators = listOf("+", "-", "×")
        return Triple(num1, num2, operators[Random.nextInt(operators.size)])
    }

    private fun generateHardProblem(): Triple<Int, Int, String> {
        val num1 = Random.nextInt(20, 101)
        val num2 = Random.nextInt(20, 101)
        val operators = listOf("+", "-", "×", "÷")
        var operator = operators[Random.nextInt(operators.size)]
        
        // Ensure division results in whole numbers
        if (operator == "÷") {
            val product = num1 * num2
            return Triple(product, num2, operator)
        }
        
        return Triple(num1, num2, operator)
    }

    private fun calculateAnswer(num1: Int, num2: Int, operator: String): Int {
        return when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> num1 / num2
            else -> throw IllegalArgumentException("Unknown operator")
        }
    }

    private fun formatProblem(num1: Int, num2: Int, operator: String): String {
        return "$num1 $operator $num2 = ?"
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerInput.text.toString().toIntOrNull()
        if (userAnswer == null) {
            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        if (userAnswer == correctAnswer) {
            Toast.makeText(context, getString(R.string.correct_answer), Toast.LENGTH_SHORT).show()
            viewModel.onMissionCompleted(args.alarmId)
            findNavController().popBackStack()
        } else {
            attemptsLeft--
            if (attemptsLeft > 0) {
                Toast.makeText(context, getString(R.string.wrong_answer), Toast.LENGTH_SHORT).show()
                binding.answerInput.text?.clear()
                updateAttemptsText()
            } else {
                // Generate new problem after all attempts are used
                attemptsLeft = 3
                generateMathProblem(args.difficulty)
                binding.answerInput.text?.clear()
                updateAttemptsText()
                Toast.makeText(context, "New problem generated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAttemptsText() {
        binding.attemptsText.text = getString(R.string.attempts_remaining, attemptsLeft)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
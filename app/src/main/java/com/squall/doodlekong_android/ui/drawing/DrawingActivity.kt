package com.squall.doodlekong_android.ui.drawing

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.squall.doodlekong_android.R
import com.squall.doodlekong_android.databinding.ActivityDrawingBinding
import com.squall.doodlekong_android.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawingBinding

    private val viewModel by viewModels<DrawingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToUiStateUpdates()

        binding.apply {
            colorGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.checkRadioButton(checkedId)
            }
        }
    }

    private fun selectColor(color: Int): Unit {
        binding.apply {
            drawingView.setColor(color)
            drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
        }
    }

    private fun subscribeToUiStateUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedColorButtonId.collect { id ->
                    binding.apply {
                        colorGroup.check(id)
                        when (id) {
                            R.id.rbBlack -> selectColor(Color.BLACK)
                            R.id.rbBlue -> selectColor(Color.BLUE)
                            R.id.rbGreen -> selectColor(Color.GREEN)
                            R.id.rbOrange -> selectColor(
                                ContextCompat.getColor(
                                    this@DrawingActivity,
                                    R.color.orange
                                )
                            )
                            R.id.rbRed -> selectColor(Color.RED)
                            R.id.rbYellow -> selectColor(Color.YELLOW)
                            R.id.rbEraser -> {
                                drawingView.setColor(Color.WHITE)
                                drawingView.setThickness(40f)
                            }
                        }
                    }
                }
            }
        }
    }
}
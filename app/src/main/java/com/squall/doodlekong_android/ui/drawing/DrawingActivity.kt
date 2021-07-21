package com.squall.doodlekong_android.ui.drawing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squall.doodlekong_android.databinding.ActivityDrawingBinding

class DrawingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
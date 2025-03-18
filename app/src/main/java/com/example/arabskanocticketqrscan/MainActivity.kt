package com.example.arabskanocticketqrscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.arabskanocticketqrscan.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        camera = Camera(this, binding.pvCamera, BarcodeAnalyzer( { qrValue ->
            binding.tvQrValue.text = qrValue
        } ))

        camera.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.stopCamera()
    }
}

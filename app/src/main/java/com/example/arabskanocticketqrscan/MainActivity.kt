package com.example.arabskanocticketqrscan

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.arabskanocticketqrscan.databinding.ActivityMainBinding
import java.io.File

class MainActivity : ComponentActivity() {

    companion object {
        private const val IS_DEBUG = true
        private const val IMED_ATTENDANTS_JSON = "jenicek_ticketmock.json"
        private const val ATTENDANTS_JSON = "attendants.json"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera
    private lateinit var attendants: TicketModel.Attendants

    private fun translateImedAttendants() {
        val imedAttendantsJson = LocalStorage.retrieveAssetContent(this, IMED_ATTENDANTS_JSON)
        val imedAttendants = ImedAttendantsBridge.parseFrom(imedAttendantsJson)
        attendants = ImedAttendantsBridge.evolve(imedAttendants)

        val attendantsJson = TicketModel.getJson(attendants)
        LocalStorage.saveContentString(this, ATTENDANTS_JSON, attendantsJson)
    }

    private fun retrieveAttendants() {
        val attendantsJsonFile = File(this.filesDir, ATTENDANTS_JSON)

        if (!attendantsJsonFile.exists())
            translateImedAttendants()
        else {
            val attendantsJson = attendantsJsonFile.reader().readText()
            attendants = TicketModel.parseFrom(attendantsJson)
        }
    }

    private fun saveAttendants() {
        val attendantsJson = TicketModel.getJson(attendants)
        LocalStorage.saveContentString(this, ATTENDANTS_JSON, attendantsJson)
    }

    private fun debugResetAttendants() {
        if (IS_DEBUG)
            translateImedAttendants()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        retrieveAttendants()

        camera = Camera(this, binding.pvCamera, BarcodeAnalyzer { qrValue ->
            val setText = { value: String, colorId: Int ->
                binding.tvQrValue.apply {
                    text = value
                    background = resources.getDrawable(colorId, theme)
                }
            }

            when (qrValue) {
                "_VELVLOUD_RESET_" -> {
                    debugResetAttendants()
                    setText("DEBUG RESET SUCCESSFUL", R.color.welcome)
                }
                else -> {
                    when (attendants.check(qrValue)) {
                        TicketModel.CheckStatus.WELCOME -> setText("$qrValue WELCOME", R.color.welcome)
                        TicketModel.CheckStatus.INTRUDER -> setText("$qrValue INTRUDER", R.color.intruder)
                        TicketModel.CheckStatus.ALIEN -> setText("$qrValue ALIEN", R.color.alien)
                    }
                }
            }
        })

        camera.startCamera()
    }

    override fun onPause() {
        super.onPause()
        saveAttendants()
    }

    override fun onResume() {
        super.onResume()
        retrieveAttendants()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.stopCamera()
    }
}

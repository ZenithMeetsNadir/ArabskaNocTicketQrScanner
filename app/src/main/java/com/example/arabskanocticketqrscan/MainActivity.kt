package com.example.arabskanocticketqrscan

import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.arabskanocticketqrscan.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera
    private lateinit var multicastLock: WifiManager.MulticastLock

    private suspend fun onRequestSuccess(checkStatus: TicketModel.CheckStatus, isCheck: Boolean) {
        withContext(Dispatchers.Main) {
            binding.apply {
                thvQrValue.checkStatus = checkStatus
                thvQrValue.displayTicket()
            }

            showStatus(checkStatus, isCheck)
        }
    }

    private fun launchCheck(ticketHash: String) {
        binding.apply {
            thvQrValue.ticketHash = ticketHash
            thvQrValue.checkStatus = TicketModel.CheckStatus.PENDING
            thvQrValue.displayTicket()
        }

        if (AttendantsRepo.tryLaunchCheckTimeout(ticketHash) { checkStatus, isCheck -> onRequestSuccess(checkStatus, isCheck) })
            showPending()
        else
            Toast.makeText(this, "request pending", Toast.LENGTH_SHORT).show()
    }

    private fun launchUncheck(ticketHash: String) {
        binding.apply {
            thvQrValue.ticketHash = ticketHash
            thvQrValue.checkStatus = TicketModel.CheckStatus.PENDING
            thvQrValue.displayTicket()
        }

        if (AttendantsRepo.tryLaunchUncheckTimeout(ticketHash) { checkStatus, isCheck -> onRequestSuccess(checkStatus, isCheck) })
            showPending()
        else
            Toast.makeText(this, "request pending", Toast.LENGTH_SHORT).show()
    }

    private fun showPending() {
        binding.apply {
            ivStatus.isVisible = false
            tvStatusMgs.isVisible = false
            tvConnecting.isVisible = true
        }
    }

    private var hideStatusJob: Job? = null

    private fun showStatus(checkStatus: TicketModel.CheckStatus, isCheck: Boolean) {
        binding.apply {
            tvConnecting.isVisible = false

            if (isCheck) {
                ivStatus.setImageDrawable(getDrawable(if (checkStatus == TicketModel.CheckStatus.WELCOME) R.drawable.success else R.drawable.error))
                ivStatus.isVisible = true

                tvStatusMgs.text = when (checkStatus) {
                    TicketModel.CheckStatus.WELCOME -> "Vítejte"
                    TicketModel.CheckStatus.INTRUDER -> "Lístek opakovaně naskenován"
                    TicketModel.CheckStatus.ALIEN -> "Lístek neexistuje"
                    else -> "Nastala chyba"
                }
                tvStatusMgs.isVisible = true

                if (hideStatusJob != null && !hideStatusJob!!.isCompleted)
                    hideStatusJob!!.cancel()

                hideStatusJob = CoroutineScope(Dispatchers.Default).launch {
                    try {
                        delay(5000)
                        withContext(Dispatchers.Main) {
                            ivStatus.isVisible = false
                            tvStatusMgs.isVisible = false
                        }
                    } catch (_: CancellationException) { }
                }
            }
        }
    }

    private fun connectToDb() {
        AttendantsRepo.url = null

        binding.tvDbConnection.text = "scanning lan for db..."

        CoroutineScope(Dispatchers.IO).launch {
            AttendantsRepo.getDbUrlTryLan { lanFound ->
                binding.tvDbConnection.text = (if (lanFound) "lan" else "fallback") + " db: ${AttendantsRepo.url!!}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }

        val wifi = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        multicastLock = wifi.createMulticastLock("udp-broadcast-lock")
        multicastLock.setReferenceCounted(true)
        multicastLock.acquire()

        AttendantsRepo.onErrorAction = { msg ->
            Log.e("attendantsRepoError", msg)
            binding.tvDbConnection.text = msg
        }

        binding.tvDbConnection.setOnClickListener {
            connectToDb()
        }

        connectToDb()

        camera = Camera(this, binding.pvCamera, BarcodeAnalyzer { qrValue ->
            launchCheck(qrValue)
        })

        binding.btnRecheck.setOnClickListener {
            launchCheck(binding.thvQrValue.ticketHash)
        }

        binding.btnUncheck.setOnClickListener {
            launchUncheck(binding.thvQrValue.ticketHash)
        }

        camera.startCamera()
    }

    override fun onPause() {
        super.onPause()
        camera.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        camera.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.stopCamera()
        multicastLock.release()
    }
}

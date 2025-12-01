package com.example.arabskanocticketqrscan

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arabskanocticketqrscan.databinding.ActivityMainBinding
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.ktor.client.*
import io.ktor.client.engine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivity : ComponentActivity() {

    companion object {
        const val IS_DEBUG = true
    }

    private val overrideJobManager = OverrideJobManager()

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera

    private val dropdownEnabled: Boolean get() {
        binding.apply {
            return AttendantsRepo.getByEmail(etEmail.text.toString()).size > 1 || thvQrValue.dropdownEnabled
        }
    }

    private fun updateSelection() {
        binding.apply {
            val tickets = AttendantsRepo.getByEmail(etEmail.text.toString())
            updateSelection(tickets.ifEmpty { null })
        }
    }

    private fun updateSelection(ticketSelection: List<String>?) {
        binding.apply {
            val hashSelectionAdapter = rvManualHashSelection.adapter as ManualHashSelectionAdapter
            hashSelectionAdapter.ticketHashes = ticketSelection
            //hashSelectionAdapter.notifyDataSetChanged()
        }
    }

    private fun flipSelectionVisibility() {
        binding.rvManualHashSelection.apply {
            if (isInvisible && dropdownEnabled) {
                updateSelection()
                visibility = View.VISIBLE
            } else if (isVisible)
                visibility = View.INVISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rvManualHashSelection.layoutManager = LinearLayoutManager(this)
        binding.apply {
            tvDbConnection.text = "scanning lan for db..."
            AttendantsRepo.onErrorAction = { msg ->
                tvDbConnection.text = msg
            }

            val job = CoroutineScope(Dispatchers.Default).launch {
                AttendantsRepo.getDbUrlTryLan { lanFound ->
                    tvDbConnection.text = (if (lanFound) "lan" else "fallback") + " db: ${AttendantsRepo.url!!}"
                }
            }

            btnCheck.isEnabled = false
            btnCheck.setOnClickListener {
                thvQrValue.check()
                btnCheck.isEnabled = false
            }

            etEmail.addTextChangedListener(OnTextChanged { s ->
                val input = s.toString()
                overrideJobManager.launchInstead({ AttendantsRepo.getByEmailSus(input) }, { tickets ->
                    if (tickets.isNotEmpty()) {
                        if (tickets.size == 1) {
                            thvQrValue.ticketHash = tickets[0]
                            btnCheck.isEnabled = true
                        } else {
                            thvQrValue.apply {
                                text = "<click to select>"
                                background = getDrawable(R.color.white)
                            }

                            updateSelection(tickets)
                        }
                    } else {
                        updateSelection(null)
                        thvQrValue.apply {
                            text = "ticket not found"
                            background = getDrawable(R.color.white)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        rvManualHashSelection.adapter?.notifyDataSetChanged()
                    }
                })
            })

            rvManualHashSelection.adapter = ManualHashSelectionAdapter { selectedTicket ->
                thvQrValue.ticketHash = selectedTicket
                rvManualHashSelection.visibility = View.INVISIBLE
                btnCheck.isEnabled = true
            }

            thvQrValue.setOnClickListener {
                flipSelectionVisibility()
            }
        }

        camera = Camera(this, binding.pvCamera, BarcodeAnalyzer { qrValue ->
            binding.apply {
                etEmail.setText(AttendantsRepo.getEmail(qrValue))

                thvQrValue.apply {
                    ticketHash = qrValue
                    check()
                }
            }
        })

        camera.startCamera()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.stopCamera()
    }
}

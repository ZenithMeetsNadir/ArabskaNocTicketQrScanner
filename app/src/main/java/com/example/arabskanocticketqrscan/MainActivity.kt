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

class MainActivity : ComponentActivity() {

    companion object {
        const val IS_DEBUG = true
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: Camera

    private lateinit var attendantsRepo: AttendantsRepo

    private fun updateSelection() {
        binding.apply {
            val tickets = attendantsRepo.attendants!!.getByEmail(etEmail.text.toString())
            updateSelection(if (tickets.isNotEmpty()) tickets else null)
        }
    }

    private fun updateSelection(ticketSelection: List<String>?) {
        binding.apply {
            val hashSelectionAdapter = (rvManualHashSelection.adapter as ManualHashSelectionAdapter)
            hashSelectionAdapter.ticketHashes = ticketSelection
            rvManualHashSelection.adapter = hashSelectionAdapter
        }
    }

    private fun flipSelectionVisibility() {
        binding.rvManualHashSelection.apply {
            if (isInvisible) {
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

        attendantsRepo = AttendantsRepo.getSingleton()
        attendantsRepo.retrieveAttendants(this)

        binding.rvManualHashSelection.layoutManager = LinearLayoutManager(this)
        binding.apply {
            btnCheck.isEnabled = false
            btnCheck.setOnClickListener {
                thvQrValue.check()
                btnCheck.isEnabled = false
            }

            etEmail.addTextChangedListener(OnTextChanged { s, _, _, _ ->
                val input = s.toString()
                when (input) {
                    TicketModel.DEBUG_RESET -> thvQrValue.text = TicketModel.DEBUG_RESET
                    else -> {
                        val tickets = attendantsRepo.attendants!!.getByEmail(input)
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
                        }
                    }
                }
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
                etEmail.setText(attendantsRepo.attendants!!.getEmail(qrValue))

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
        attendantsRepo.saveAttendants(this)
    }

    override fun onResume() {
        super.onResume()
        attendantsRepo.retrieveAttendants(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.stopCamera()
    }
}

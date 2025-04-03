package com.example.arabskanocticketqrscan

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.toString

class TicketHashView : TextView {

    init {
        background = ContextCompat.getDrawable(context, R.color.white)
    }

    private var _attendantsRepo: AttendantsRepo? = null
    protected val attendantsRepo: AttendantsRepo get() {
        if (_attendantsRepo == null)
            _attendantsRepo = AttendantsRepo.getSingleton()

        return _attendantsRepo!!
    }

    var ticketHash: String = ""
        set(value) {
            field = value
            displayTicket()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun check() {
        if (attendantsRepo.attendants != null) {
            val checkStatus = attendantsRepo.attendants!!.check(ticketHash)
            if (checkStatus == TicketModel.CheckStatus.WELCOME) {
                text = "$text WELCOME"
                displayToastMessage(checkStatus)
            }

            validateBg(R.color.welcome, checkStatus)
        }
    }

    private fun displayToastMessage(checkStatus: TicketModel.CheckStatus) {
        Toast.makeText(context, when (checkStatus) {
            TicketModel.CheckStatus.WELCOME -> "WELCOME"
            TicketModel.CheckStatus.INTRUDER -> "Ticket has already been checked"
            TicketModel.CheckStatus.ALIEN -> "Ticket is invalid"
        }, Toast.LENGTH_LONG).show()
    }

    private fun validateBg(welcomeColorId: Int, checkStatus: TicketModel.CheckStatus? = null) {
        if (attendantsRepo.attendants != null) {
            var mutCheckStatus = checkStatus
            if (mutCheckStatus == null)
                mutCheckStatus = attendantsRepo.attendants!!.scan(ticketHash)

            background = ContextCompat.getDrawable(context, when (mutCheckStatus) {
                TicketModel.CheckStatus.WELCOME -> welcomeColorId
                TicketModel.CheckStatus.INTRUDER -> R.color.intruder
                TicketModel.CheckStatus.ALIEN -> R.color.alien
            })
        }
    }

    fun displayTicket() {
        Log.d("bindView", attendantsRepo.attendants.toString())
        if (attendantsRepo.attendants != null) {
            validateBg(R.color.white)

            text = when (attendantsRepo.attendants!!.scan(ticketHash)) {
                TicketModel.CheckStatus.WELCOME -> ticketHash
                TicketModel.CheckStatus.INTRUDER -> "$ticketHash INTRUDER"
                TicketModel.CheckStatus.ALIEN -> "$ticketHash ALIEN"
            }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (attendantsRepo.attendants == null) {
            super.setText(text, type)
            return
        }

        when (text.toString()) {
            TicketModel.DEBUG_RESET -> {
                attendantsRepo.debugResetAttendants(context)
                super.setText("DEBUG RESET SUCCESSFUL", type)
                background = ContextCompat.getDrawable(context, R.color.welcome)
            }
            else -> {
                super.setText(text, type)
            }
        }
    }
}
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

    var checkStatusCache: TicketModel.CheckStatus? = null

    init {
        background = ContextCompat.getDrawable(context, R.color.white)
    }

    val dropdownEnabled: Boolean get() {
        val email = AttendantsRepo.getEmail(ticketHash)
        return AttendantsRepo.getByEmail(email).size > 1
    }

    var ticketHash: String = ""
        set(value) {
            field = value
            displayTicket()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun consumeCheckStatusCache(): TicketModel.CheckStatus? {
        val status = checkStatusCache
        checkStatusCache = null
        return status
    }

    fun check() {
        val checkStatus = AttendantsRepo.check(ticketHash)

        //checkStatusCache = checkStatus
        displayToastMessage(checkStatus)
        validateBg(R.color.welcome, checkStatus)
    }

    private fun displayToastMessage(checkStatus: TicketModel.CheckStatus) {
        Toast.makeText(context, when (checkStatus) {
            TicketModel.CheckStatus.WELCOME -> "WELCOME"
            TicketModel.CheckStatus.INTRUDER -> "Ticket has already been checked"
            TicketModel.CheckStatus.ALIEN -> "Ticket is invalid"
            TicketModel.CheckStatus.ERROR -> "An error occurred while checking ticket"
        }, Toast.LENGTH_LONG).show()
    }

    private fun validateBg(welcomeColorId: Int, checkStatus: TicketModel.CheckStatus? = null) {
        var mutCheckStatus = checkStatus
        if (mutCheckStatus == null)
            mutCheckStatus = AttendantsRepo.scan(ticketHash)

        background = ContextCompat.getDrawable(context, when (mutCheckStatus) {
            TicketModel.CheckStatus.WELCOME -> welcomeColorId
            TicketModel.CheckStatus.INTRUDER -> R.color.intruder
            TicketModel.CheckStatus.ALIEN -> R.color.alien
            TicketModel.CheckStatus.ERROR -> R.color.alien
        })
    }

    fun displayTicket() {
        validateBg(R.color.white)
        var checkStatus = consumeCheckStatusCache()
        if (checkStatus == null)
            checkStatus = AttendantsRepo.scan(ticketHash)

        text = "$ticketHash " + when (checkStatus) {
            TicketModel.CheckStatus.WELCOME -> "WELCOME"
            TicketModel.CheckStatus.INTRUDER -> "INTRUDER"
            TicketModel.CheckStatus.ALIEN -> "ALIEN"
            TicketModel.CheckStatus.ERROR -> "ERROR"
        }
    }
}
package com.example.arabskanocticketqrscan

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.Checkbox
import androidx.core.content.ContextCompat
import kotlin.toString

class TicketHashView : TextView {

    init {
        background = ContextCompat.getDrawable(context, R.color.white)
    }

    var checkStatus = TicketModel.CheckStatus.PENDING

    var ticketHash: String = ""

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun displayTicket() {
        text = "$ticketHash " + when (checkStatus) {
            TicketModel.CheckStatus.WELCOME -> ""
            TicketModel.CheckStatus.INTRUDER -> "INTRUDER"
            TicketModel.CheckStatus.ALIEN -> "ALIEN"
            TicketModel.CheckStatus.PENDING -> "PENDING"
            TicketModel.CheckStatus.ERROR -> "ERROR"
        }

        background = ContextCompat.getDrawable(context, when (checkStatus) {
            TicketModel.CheckStatus.WELCOME -> R.color.welcome
            TicketModel.CheckStatus.INTRUDER -> R.color.intruder
            TicketModel.CheckStatus.ALIEN -> R.color.alien
            TicketModel.CheckStatus.PENDING -> R.color.white
            TicketModel.CheckStatus.ERROR -> R.color.alien
        })
    }
}
package com.example.arabskanocticketqrscan

import android.util.Log
import com.google.gson.Gson

class TicketModel {

    enum class CheckStatus {
        WELCOME,
        INTRUDER,
        ALIEN
    }

    class Attendants(
        val attendants: List<Attendant>
    ) {
        fun check(ticket: String): CheckStatus {
            val attendant = attendants.find { attendant -> attendant.ticket == ticket }
            if (attendant != null) {
                if (attendant.valid) {
                    attendant.valid = false
                    return CheckStatus.WELCOME
                }

                return CheckStatus.INTRUDER
            }

            return CheckStatus.ALIEN
        }

        fun scan(ticket: String): CheckStatus {
            val attendant = attendants.find { attendant -> attendant.ticket == ticket }
            if (attendant != null)
                return if (attendant.valid) CheckStatus.WELCOME else CheckStatus.INTRUDER

            return CheckStatus.ALIEN
        }

        fun getByEmail(email: String): List<String> {
            return attendants
                .filter { attendant -> attendant.email == email }
                .map { attendant -> attendant.ticket }
        }

        fun getEmail(ticketHash: String): String {
            return attendants.find { attendant -> attendant.ticket == ticketHash }?.email ?: ""
        }
    }

    data class Attendant(
        val email: String,
        val ticket: String,
        var valid: Boolean = true,
    )

    companion object : IFromJson<Attendants> {

        const val DEBUG_RESET = "_VELVLOUD_RESET_"

        private val gson = Gson()

        override fun parseFrom(json: String): Attendants {
            return gson.fromJson(json, Attendants::class.java)
        }

        override fun getJson(obj: Attendants): String {
            return gson.toJson(obj)
        }
    }
}
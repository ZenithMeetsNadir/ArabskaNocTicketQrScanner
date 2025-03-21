package com.example.arabskanocticketqrscan

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
    }

    data class Attendant(
        val email: String,
        val ticket: String,
        var valid: Boolean = true,
    )

    companion object : IFromJson<Attendants> {
        val preparsed = Attendants(listOf(
            Attendant("josifek.rak@seznam.cz", "4c5ea96f"),
            Attendant("josifek.rak@seznam.cz", "0b88fa6a"),
            Attendant("josifek.rak@seznam.cz", "90c4ee5d"),
            Attendant("josifek.rak@seznam.cz", "69e62169")
        ))

        private val gson = Gson()

        override fun parseFrom(json: String): Attendants {
            return gson.fromJson(json, Attendants::class.java)
        }

        override fun getJson(obj: Attendants): String {
            return gson.toJson(obj)
        }
    }
}
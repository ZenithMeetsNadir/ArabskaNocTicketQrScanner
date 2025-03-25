package com.example.arabskanocticketqrscan

import com.google.gson.Gson

class ImedAttendantsBridge {

    data class AttendantsRaw(
        val data: List<TicketHolder>
    )

    data class TicketHolder(
        val address: String,
        val hashes: List<String>,
        val transaction_hash: String,
        val deleted: Boolean
    )

    companion object : IFromJson<AttendantsRaw> {
        private val gson: Gson = Gson()

        fun evolve(raw : AttendantsRaw): TicketModel.Attendants {
            val attendants = ArrayList<TicketModel.Attendant>()

            for (ticketHolder in raw.data) {
                if (!ticketHolder.deleted) {
                    for (ticket in ticketHolder.hashes)
                        attendants.add(TicketModel.Attendant(ticketHolder.address, ticket))
                }
            }

            return TicketModel.Attendants(attendants)
        }

        override fun parseFrom(json: String): AttendantsRaw {
            return gson.fromJson(json, AttendantsRaw::class.java)
        }

        override fun getJson(obj: AttendantsRaw): String {
            return gson.toJson(obj)
        }
    }
}
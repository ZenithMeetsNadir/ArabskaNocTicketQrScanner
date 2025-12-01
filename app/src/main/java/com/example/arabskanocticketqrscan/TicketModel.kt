package com.example.arabskanocticketqrscan

class TicketModel {

    enum class CheckStatus {
        WELCOME,
        INTRUDER,
        ALIEN,
        ERROR,
    }

    data class TicketHolder(
        val address: String,
        val hashes: List<String>,
        val manual: Boolean,
        val deleted: Boolean,
        val seen: List<Int>,
    ) {
        companion object : IDeserialize<TicketHolder> {
            override fun fromJson(json: String): TicketHolder {
                return DI.gson.fromJson(json, TicketHolder::class.java)
            }
        }

        fun seen(ticket: String): Boolean {
            return seen.contains(hashes.indexOf(ticket))
        }
    }

    data class GetByEmailResult(
        val records: List<TicketHolder>
    ) {
        companion object : IDeserialize<GetByEmailResult> {
            override fun fromJson(json: String): GetByEmailResult {
                return DI.gson.fromJson(json, GetByEmailResult::class.java)
            }
        }
    }
}
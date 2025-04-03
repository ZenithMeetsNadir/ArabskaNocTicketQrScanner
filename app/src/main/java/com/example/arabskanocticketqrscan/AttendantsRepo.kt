package com.example.arabskanocticketqrscan

import android.content.Context
import java.io.File

class AttendantsRepo() {

    companion object {
        protected var instance: AttendantsRepo? = null

        fun getSingleton(): AttendantsRepo {
            if (instance == null)
                instance = AttendantsRepo()

            return instance!!
        }

        private const val IMED_ATTENDANTS_JSON = "jenicek_ticketmock.json"
        private const val ATTENDANTS_JSON = "attendants.json"
    }

    var attendants: TicketModel.Attendants? = null

    fun translateImedAttendants(context: Context) {
        val imedAttendantsJson = LocalStorage.retrieveFileContent(context, IMED_ATTENDANTS_JSON)
        val imedAttendants = ImedAttendantsBridge.parseFrom(imedAttendantsJson)
        attendants = ImedAttendantsBridge.evolve(imedAttendants)

        saveAttendants(context)
    }

    fun retrieveAttendants(context: Context) {
        LocalStorage.copyToInternalStorage(context, IMED_ATTENDANTS_JSON, IMED_ATTENDANTS_JSON)
        val attendantsJsonFile = File(context.filesDir, ATTENDANTS_JSON)

        if (attendantsJsonFile.exists() && attendantsJsonFile.length() == 0L)
            attendantsJsonFile.delete()

        if (!attendantsJsonFile.exists())
            translateImedAttendants(context)
        else {
            val attendantsJson = attendantsJsonFile.reader().readText()
            attendants = TicketModel.parseFrom(attendantsJson)
        }
    }

    fun saveAttendants(context: Context) {
        if (attendants != null) {
            val attendantsJson = TicketModel.getJson(attendants!!)
            LocalStorage.saveContentString(context, ATTENDANTS_JSON, attendantsJson)
        }
    }

    fun debugResetAttendants(context: Context) {
        if (MainActivity.IS_DEBUG)
            translateImedAttendants(context)
    }
}
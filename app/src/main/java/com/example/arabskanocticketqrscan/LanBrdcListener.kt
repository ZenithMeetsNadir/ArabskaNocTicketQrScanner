package com.example.arabskanocticketqrscan

import android.util.Log
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import kotlin.time.Duration

class LanBrdcListener {

    private val serverSock: BoundDatagramSocket

    constructor(port: Int) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        serverSock = runBlocking {
            aSocket(selectorManager).udp()
                .bind("0.0.0.0", port)
        }
    }

    data class DbServer(
        val serverAddr: SocketAddress,
        val port: Int,
    )

    fun close() {
        serverSock.close()
    }

    suspend fun listenForDbBrdc(): DbServer {
        while (true) {
            val datagram = serverSock.receive()
            try {
                val port = datagram.packet.readText().toInt()
                return DbServer(datagram.address, port)
            } catch (e: Exception) {
                val msg = if (e.message != null) e.message!! else ""
                Log.e("brdcast", msg)
            }
        }
    }

    suspend fun listenForDbBrdcTimeout(timeout: Long): DbServer {
        return kotlinx.coroutines.withTimeout(timeout) {
            listenForDbBrdc()
        }
    }
}
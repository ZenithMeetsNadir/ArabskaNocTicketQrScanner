package com.example.arabskanocticketqrscan

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.http.buildUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

object AttendantsRepo {

    private val brdcPort = 6769
    private val defaultUrl = "http://jenyyk.duckdns.org:6767"

    @Volatile
    var url: String? = null
    @Volatile
    private var searching = false

    private val httpClient: HttpClient = HttpClient(CIO)

    var onErrorAction: ((msg: String) -> Unit)? = null

    suspend fun getDbUrlTryLan(onUrlFetched: (lanFound: Boolean) -> Unit = {}): String {
        if (url == null) {
            if (!searching) {
                searching = true

                val listener = LanBrdcListener(brdcPort)
                var lanFound = false
                url = try {
                    listener.listenForDbBrdcTimeout(3000L).let {
                        val tempUrl = "http:/${it.serverAddr.toString().split(':')[0]}:${it.port}"
                        lanFound = true
                        tempUrl
                    }
                } catch (_: TimeoutCancellationException) {
                    defaultUrl
                }

                listener.close()
                onUrlFetched(lanFound)
                searching = false
            } else {
                while (url == null)
                    delay(500L)
            }
        }

        return url!!
    }

    suspend fun check(ticket: String): TicketModel.CheckStatus {
        val resp = try {
            val url = "${ getDbUrlTryLan() }/ticket/$ticket"
            httpClient.patch(url)
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return TicketModel.CheckStatus.ERROR
        }

        return when (resp.status.value) {
            200 -> TicketModel.CheckStatus.WELCOME
            404 -> TicketModel.CheckStatus.ALIEN
            409 -> TicketModel.CheckStatus.INTRUDER
            else -> TicketModel.CheckStatus.ERROR
        }
    }

    const val timeoutMs: Long = 5000

    @Volatile
    var peppermintSyrupyPawjob: Job? = null
    @Volatile
    var peppermintSyrupyPawjobRunning: Boolean = false

    fun tryLaunchCheckTimeout(ticket: String, onReqSuccess: suspend (checkStatus: TicketModel.CheckStatus, isCheck: Boolean) -> Unit): Boolean {
        if (peppermintSyrupyPawjobRunning)
            return false

        peppermintSyrupyPawjobRunning = true

        peppermintSyrupyPawjob = CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(timeoutMs) { onReqSuccess(check(ticket), true) }
            }
            catch (_: Exception) {
                onReqSuccess(TicketModel.CheckStatus.ERROR, true)
            }

            peppermintSyrupyPawjobRunning = false
        }

        return true
    }

    suspend fun uncheck(ticket: String): TicketModel.CheckStatus {
        val resp = try {
            val url = "${getDbUrlTryLan()}/ticket/$ticket"
            httpClient.delete(url)
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return TicketModel.CheckStatus.ERROR
        }

        return when (resp.status.value) {
            200 -> TicketModel.CheckStatus.WELCOME
            404 -> TicketModel.CheckStatus.ALIEN
            else -> TicketModel.CheckStatus.ERROR
        }
    }

    fun tryLaunchUncheckTimeout(ticket: String, onReqSuccess: suspend (checkStatus: TicketModel.CheckStatus, isCheck: Boolean) -> Unit): Boolean {
        if (peppermintSyrupyPawjobRunning)
            return false

        peppermintSyrupyPawjobRunning = true

        peppermintSyrupyPawjob = CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(timeoutMs) { onReqSuccess(uncheck(ticket), false) }
            } catch (_: Exception) {
                onReqSuccess(TicketModel.CheckStatus.ERROR, false)
            }

            peppermintSyrupyPawjobRunning = false
        }

        return true
    }
}
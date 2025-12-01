package com.example.arabskanocticketqrscan

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.ConnectException
import java.net.URLEncoder

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
                        val tempUrl = "${it.serverAddr}:${it.port}"
                        lanFound = true
                        tempUrl
                    }
                } catch (_: TimeoutCancellationException) {
                    defaultUrl
                }

                onUrlFetched(lanFound)
            } else {
                while (url == null)
                    delay(500L)
            }
        }

        return url!!
    }

    fun check(ticket: String): TicketModel.CheckStatus {
        val resp = try {
            runBlocking {
                val url = "${ getDbUrlTryLan() }/ticket/$ticket"
                httpClient.patch(url)
            }
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return TicketModel.CheckStatus.ERROR
        }

        return when (resp.status.value) {
            200 -> TicketModel.CheckStatus.WELCOME
            404 -> TicketModel.CheckStatus.ALIEN
            409 -> TicketModel.CheckStatus.INTRUDER
            else -> TicketModel.CheckStatus.ALIEN
        }
    }

    fun uncheck(ticket: String) {
        try {
            runBlocking {
                val url = "${getDbUrlTryLan()}/ticket/$ticket"
                httpClient.delete(url)
            }
        } catch (e: ConnectException) {
            onErrorAction?.invoke("request error: ${e.message}")
        }
    }

    fun scan(ticket: String): TicketModel.CheckStatus {
        val resp = try {
            runBlocking {
             val url = "${ getDbUrlTryLan() }/ticket/$ticket"
             httpClient.get(url)
            }
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return TicketModel.CheckStatus.ERROR
        }

        return when (resp.status.value) {
            200 -> {
                val json = runBlocking { resp.bodyAsText() }
                val holder = TicketModel.TicketHolder.fromJson(json)
                if (holder.seen(ticket))
                    TicketModel.CheckStatus.INTRUDER
                else
                    TicketModel.CheckStatus.WELCOME
            }
            else -> TicketModel.CheckStatus.ALIEN
        }
    }

    fun getEmail(ticket: String): String {
        val resp = try {
            runBlocking {
                val url = "${ getDbUrlTryLan() }/ticket/$ticket"
                httpClient.get(url)
            }
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return "error"
        }

        if (resp.status.value == 200) {
            val json = runBlocking { resp.bodyAsText() }
            val holder = TicketModel.TicketHolder.fromJson(json)
            return holder.address
        }

        return "email not found"
    }

    fun getByEmail(email: String): List<String> {
        return runBlocking { getByEmailSus(email) }
    }

    suspend fun getByEmailSus(email: String): List<String> {
        val emailEnc = URLEncoder.encode(email, "UTF-8")

        val resp = try {
            val url = " ${ getDbUrlTryLan() }/email/$emailEnc"
            httpClient.get(url)
        } catch (e: Exception) {
            onErrorAction?.invoke("request error: ${e.message}")
            return listOf()
        }

        if (resp.status.value == 200) {
            var json = resp.bodyAsText()
            if (!(json.isNotEmpty() && json.first() == '{' && json.last() == '}'))
                json = "{records:$json}"

            Log.d("respJson", json)

            val resArr = ArrayList<String>()
            TicketModel.GetByEmailResult.fromJson(json).records.forEach { it ->
                resArr.addAll(it.hashes)
            }

            return resArr
        }

        return listOf()
    }
}
package hu.bme.hit.android.espcontroller.network

import android.util.Log
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

object NetworkHandler {
    private val timeStamps = listOf("MIDNIGHT", "MORNING", "NOON", "EVENING")
    private lateinit var webSocket: WebSocket
    private var isWebSocketOpen = false
    private val messageChannel = Channel<String>(Channel.BUFFERED)

    fun initialization(ipAddress: String) {
        val client = OkHttpClient.Builder().connectTimeout(5000, TimeUnit.SECONDS).build()
        val request = okhttp3.Request.Builder().url("ws://$ipAddress:81").build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                isWebSocketOpen = true
                //Log.i("OPEN", "WebSocket successfully opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                //Log.e("NetworkHandler", text)
                val sendResult = messageChannel.trySend(text)
                if (!sendResult.isSuccess) {
                    Log.e("NetworkHandler", "Failed to send message: $text")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isWebSocketOpen = false
            }

            override fun onFailure(
                webSocket: WebSocket, t: Throwable, response: okhttp3.Response?
            ) {
                isWebSocketOpen = false
                //Log.e("FAIL", "WebSocket failed: ${t.message}")
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }

    fun closeWebSocket() {
        webSocket!!.close(1000, "Closing")
    }

    fun isWebSocketOpen(): Boolean {
        return isWebSocketOpen
    }

    fun getMessageChannel(): Channel<String> {
        return messageChannel
    }

    fun getTimeStamps(): List<String> {
        return timeStamps
    }

    fun isPrivateIP(ipAddress: String): Boolean {
        val parts = ipAddress.split(".")
        if (parts.size != 4) return false

        if (parts[0] != "192" || parts[1] != "168") return false

        for (i in 2..3) {
            val intPart = parts[i].toInt()
            if (intPart < 0 || intPart > 255) return false
        }

        return true
    }
}
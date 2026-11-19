package com.payra.calling.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class NetState { Online, Offline, Connecting }

/** Watches the phone's real internet connection. Screens show a banner while [state] is not Online. */
object NetworkMonitor {
    var state by mutableStateOf(NetState.Online)
        private set

    private var started = false
    private val handler = Handler(Looper.getMainLooper())

    private fun hasInternet(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun start(context: Context) {
        if (started) return
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        started = true
        state = if (hasInternet(cm)) NetState.Online else NetState.Offline
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handler.post {
                    if (state == NetState.Offline) {
                        // The connection is back: show "Connecting..." for a moment, then everything is normal.
                        state = NetState.Connecting
                        handler.postDelayed({ if (state == NetState.Connecting) state = NetState.Online }, 2200)
                    }
                }
            }

            override fun onLost(network: Network) {
                handler.post { if (!hasInternet(cm)) state = NetState.Offline }
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                cm.registerDefaultNetworkCallback(callback)
            } else {
                cm.registerNetworkCallback(
                    NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                    callback,
                )
            }
        } catch (e: Exception) {
            Log.w("PayraNet", "could not watch the connection", e)
        }
    }
}

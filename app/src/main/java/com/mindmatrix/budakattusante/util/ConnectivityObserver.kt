package com.mindmatrix.budakattusante.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Requirement 7: Real-time internet detection.
 * Observes network connectivity status including WiFi and Mobile data.
 */
interface ConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Available, Unavailable, Losing, Lost, Wifi, Mobile
    }
}

/**
 * Requirement 7: Network Connectivity Observer Implementation.
 * Detects WiFi/mobile data and real-time internet availability.
 */
class NetworkConnectivityObserver(
    private val context: Context
): ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            fun getCurrentStatus(): ConnectivityObserver.Status {
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                return when {
                    capabilities == null -> ConnectivityObserver.Status.Unavailable
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectivityObserver.Status.Wifi
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectivityObserver.Status.Mobile
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectivityObserver.Status.Available
                    else -> ConnectivityObserver.Status.Unavailable
                }
            }

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(getCurrentStatus()) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(ConnectivityObserver.Status.Losing) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.Lost) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.Unavailable) }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    launch { send(getCurrentStatus()) }
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)
            
            // Initial check
            launch { send(getCurrentStatus()) }

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}

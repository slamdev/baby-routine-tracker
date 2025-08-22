package com.github.slamdev.babyroutinetracker.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Service for monitoring network connectivity status
 */
class NetworkConnectivityService(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkConnectivityService"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Get current network connectivity status
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Flow that emits network connectivity status changes
     */
    fun networkStatusFlow(): Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available: $network")
                trySend(NetworkStatus.Available)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                Log.d(TAG, "Network capabilities changed: hasInternet=$hasInternet, isValidated=$isValidated")
                
                if (hasInternet && isValidated) {
                    trySend(NetworkStatus.Available)
                } else {
                    trySend(NetworkStatus.Unavailable)
                }
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost: $network")
                trySend(NetworkStatus.Unavailable)
            }
            
            override fun onUnavailable() {
                Log.d(TAG, "Network unavailable")
                trySend(NetworkStatus.Unavailable)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Emit initial state
        trySend(if (isNetworkAvailable()) NetworkStatus.Available else NetworkStatus.Unavailable)
        
        awaitClose {
            Log.d(TAG, "Unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

/**
 * Represents network connectivity status
 */
sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
    
    val isAvailable: Boolean get() = this is Available
    val isUnavailable: Boolean get() = this is Unavailable
}

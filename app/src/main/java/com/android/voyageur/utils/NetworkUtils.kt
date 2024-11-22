package com.android.voyageur.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Represents the current state of the network connection.
 * [Available] when the device is connected to the internet, [Unavailable] otherwise.
 */
sealed class ConnectionState {
  object Available : ConnectionState()

  object Unavailable : ConnectionState()
}

/**
 * Returns the current connectivity state of the device.
 * [ConnectionState.Available] when the device is connected to the internet, [ConnectionState.Unavailable] otherwise.
 * @receiver Context The context to get the connectivity state from.
 * @return ConnectionState The current connectivity state.
 */
val Context.currentConnectivityState: ConnectionState
  get() {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return getCurrentConnectivityState(connectivityManager)
  }

/**
 * Returns the current connectivity state of the device.
 * [ConnectionState.Available] when the device is connected to the internet, [ConnectionState.Unavailable] otherwise.
 * @param connectivityManager The connectivity manager to get the connectivity state from.
 * @return ConnectionState The current connectivity state.
 */
private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): ConnectionState {
  val connected =
      connectivityManager.allNetworks.any { network ->
        connectivityManager
            .getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
      }

  return if (connected) ConnectionState.Available else ConnectionState.Unavailable
}

/**
 * Observes the connectivity state of the device as a flow.
 * @receiver Context The context to observe the connectivity state from.
 * @return Flow<ConnectionState> A flow that emits the current connectivity state of the device.
 * @sample observeConnectivityAsFlow
 * @see ConnectionState
 */
@ExperimentalCoroutinesApi
fun Context.observeConnectivityAsFlow() = callbackFlow {
  val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  val callback = NetworkCallback { connectionState -> trySend(connectionState) }

  val networkRequest =
      NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

  connectivityManager.registerNetworkCallback(networkRequest, callback)

  // Set current state
  val currentState = getCurrentConnectivityState(connectivityManager)
  trySend(currentState)

  // Remove callback when not used
  awaitClose {
    // Remove listeners
    connectivityManager.unregisterNetworkCallback(callback)
  }
}

/**
 * A callback that listens to network changes and emits the current connectivity state.
 * @param callback The callback to be invoked when the network state changes.
 * @return NetworkCallback The network callback.
 */
fun NetworkCallback(callback: (ConnectionState) -> Unit): ConnectivityManager.NetworkCallback {
  return object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      callback(ConnectionState.Available)
    }

    override fun onLost(network: Network) {
      callback(ConnectionState.Unavailable)
    }
  }
}

/**
 * Returns the current connectivity state of the device.
 * [ConnectionState.Available] when the device is connected to the internet, [ConnectionState.Unavailable] otherwise.
 */
@ExperimentalCoroutinesApi
@Composable
fun connectivityState(): State<ConnectionState> {
  val context = LocalContext.current

  // Creates a State<ConnectionState> with current connectivity state as initial value
  return produceState(initialValue = context.currentConnectivityState) {
    // In a coroutine, can make suspend calls
    context.observeConnectivityAsFlow().collect { value = it }
  }
}

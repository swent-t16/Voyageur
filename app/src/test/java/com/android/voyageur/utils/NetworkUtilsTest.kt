@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.voyageur.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkUtilsTest {

  private lateinit var context: Context
  private lateinit var connectivityManager: ConnectivityManager
  private lateinit var network: Network
  private lateinit var networkCapabilities: NetworkCapabilities

  @Before
  fun setUp() {
    context = mock(Context::class.java)
    connectivityManager = mock(ConnectivityManager::class.java)
    network = mock(Network::class.java)
    networkCapabilities = mock(NetworkCapabilities::class.java)

    // Mock connectivity manager in the context
    `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
  }

  @After
  fun tearDown() {
    reset(context, connectivityManager, network, networkCapabilities)
  }

  @Test
  fun `currentConnectivityState should return Available when internet is available`() {
    // Arrange
    `when`(connectivityManager.allNetworks).thenReturn(arrayOf(network))
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        .thenReturn(true)

    // Act
    val state = context.currentConnectivityState

    // Assert
    assertEquals(ConnectionState.Available, state)
  }

  @Test
  fun `currentConnectivityState should return Unavailable when no internet is available`() {
    // Arrange
    `when`(connectivityManager.allNetworks).thenReturn(arrayOf(network))
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        .thenReturn(false)

    // Act
    val state = context.currentConnectivityState

    // Assert
    assertEquals(ConnectionState.Unavailable, state)
  }

  @Test
  fun `observeConnectivityAsFlow should emit Available when internet is available`() = runTest {
    // Arrange
    `when`(connectivityManager.allNetworks).thenReturn(arrayOf(network))
    `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        .thenReturn(true)

    val callbackCaptor = argumentCaptor<ConnectivityManager.NetworkCallback>()
    doNothing()
        .`when`(connectivityManager)
        .registerNetworkCallback(any<NetworkRequest>(), callbackCaptor.capture())
    doNothing()
        .`when`(connectivityManager)
        .unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())

    // Act
    val flow = context.observeConnectivityAsFlow()
    val state = flow.first()

    // Simulate network becoming available
    callbackCaptor.firstValue.onAvailable(network)

    // Assert
    assertEquals(ConnectionState.Available, state)
  }

  @Test
  fun `observeConnectivityAsFlow should emit Unavailable when internet is lost`() = runTest {
    // Arrange
    `when`(connectivityManager.allNetworks).thenReturn(emptyArray())
    `when`(connectivityManager.getNetworkCapabilities(any())).thenReturn(null)

    val callbackCaptor = argumentCaptor<ConnectivityManager.NetworkCallback>()
    doNothing()
        .`when`(connectivityManager)
        .registerNetworkCallback(any<NetworkRequest>(), callbackCaptor.capture())
    doNothing()
        .`when`(connectivityManager)
        .unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())

    // Act
    val flow = context.observeConnectivityAsFlow()
    val state = flow.first()

    // Simulate network loss
    callbackCaptor.firstValue.onLost(network)

    // Assert
    assertEquals(ConnectionState.Unavailable, state)
  }
}

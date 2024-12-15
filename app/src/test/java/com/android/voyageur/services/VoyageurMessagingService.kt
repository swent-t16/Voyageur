package com.android.voyageur.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.android.voyageur.R
import com.android.voyageur.ui.notifications.NotificationHelper
import com.google.firebase.messaging.RemoteMessage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MessagingServiceTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var notificationManager: NotificationManager

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @Mock
    private lateinit var remoteMessage: RemoteMessage

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
    }

    @Test
    fun `test notification channel creation`() {
        // Given
        val channelCaptor = argumentCaptor<NotificationChannel>()

        // When
        NotificationHelper.createNotificationChannel(context, notificationManager)

        // Then
        verify(notificationManager).createNotificationChannel(channelCaptor.capture())
    }

    @Test
    fun `test FCM token storage`() {
        // Given
        val token = "test-token"

        // When
        saveFcmToken(context, token)

        // Then
        verify(editor).putString("fcm_token", token)
        verify(editor).apply()
    }

    // Helper functions that replicate the service logic without Firebase dependencies
    private fun saveFcmToken(context: Context, token: String) {
        context.getSharedPreferences("voyageur_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    private fun handleNotification(context: Context, remoteMessage: RemoteMessage) {
        remoteMessage.data.let { data ->
            when (data["type"]) {
                "friend_request" -> {
                    val senderName = data["senderName"] ?: "Unknown"
                }
            }
        }
    }
}
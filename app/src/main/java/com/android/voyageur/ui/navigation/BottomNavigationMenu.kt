package com.android.voyageur.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String?,
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel
) {
  val userNotifications = userViewModel.notificationCount?.collectAsState(initial = 0)?.value ?: 0
  val tripInvites = tripsViewModel.tripNotificationCount?.collectAsState(initial = 0)?.value ?: 0
  val totalNotifications = userNotifications + tripInvites

  LaunchedEffect(userNotifications, tripInvites) {
    userViewModel.getFriendRequests {}
    tripsViewModel.fetchTripInvites()
  }
  LaunchedEffect(Unit) {
    if (Firebase.auth.uid != null) {
      userViewModel.getNotificationsCount {}
      tripsViewModel.getNotificationsCount {}
    }
  }

  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(60.dp).testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          NavigationBarItem(
              icon = {
                if (tab.route == Route.PROFILE && totalNotifications > 0) {
                  Box(
                      modifier =
                          Modifier.clip(RoundedCornerShape(50))
                              .background(MaterialTheme.colorScheme.error)
                              .size(16.dp)
                              .testTag("notificationBadge")) {
                        Text(
                            text = totalNotifications.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.align(Alignment.Center))
                      }
                }
                Icon(tab.icon, contentDescription = null)
              },
              label = { Text(tab.textId) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(RoundedCornerShape(50.dp)).testTag(tab.textId))
        }
      },
  )
}

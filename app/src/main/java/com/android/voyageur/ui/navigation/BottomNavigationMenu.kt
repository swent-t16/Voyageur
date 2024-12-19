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

/**
 * A composable bottom navigation menu for the app, allowing users to navigate between
 * different sections (Overview, Search, Profile). The menu displays a list of tabs,
 * each with an icon and label. If there are any notifications (such as trip invites or
 * friend requests), a badge with the notification count will be shown on the profile tab.
 *
 * @param onTabSelect A lambda function that is called when a tab is selected. It receives the
 * [TopLevelDestination] as a parameter to navigate to the corresponding screen.
 * @param tabList A list of [TopLevelDestination] representing the tabs in the navigation menu.
 * Each tab contains information like the route, icon, and label for the tab.
 * @param selectedItem The route of the currently selected tab. This is used to highlight the
 * selected tab.
 * @param userViewModel The [UserViewModel] used to manage user-related data, including
 * notifications for friend requests and user activity.
 * @param tripsViewModel The [TripsViewModel] used to manage trip-related data, including
 * notifications for trip invites.
 */
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

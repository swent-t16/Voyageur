package com.android.voyageur.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.user.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String?,
    userViewModel: UserViewModel
) {
  val notifications by userViewModel._notificationCount.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  var isPolling by remember { mutableStateOf(true) }

  if (Firebase.auth.uid != null) {
    userViewModel.getNotificationsCount { if (it > 0) userViewModel.getFriendRequests {} }
  }
  NavigationBar(
      modifier = Modifier.fillMaxWidth().height(60.dp).testTag("bottomNavigationMenu"),
      containerColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          NavigationBarItem(
              icon = {
                if (tab.route == Route.PROFILE && notifications > 0) {
                  Box(
                      modifier =
                          Modifier.clip(RoundedCornerShape(50))
                              .background(MaterialTheme.colorScheme.error)
                              .size(16.dp)) {
                        Text(
                            text = notifications.toString(),
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

package com.android.sample.ui.search

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.navigation.BottomNavigationMenu
import com.android.sample.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.sample.ui.navigation.NavigationActions

@Composable
fun SearchScreen(
    navigationActions: NavigationActions,
) {

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Text(
            modifier = Modifier.padding(pd).testTag("emptySearchPrompt"),
            text = "You aren't able to search for anything yet.")
      })
}

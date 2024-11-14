package com.android.voyageur.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object Route {
  const val OVERVIEW = "Overview"
  const val SEARCH = "Search"
  const val PROFILE = "Profile"
  const val EDIT_PROFILE = "Edit Profile Screen"
  const val AUTH = "Auth"
  const val TOP_TABS = "TopTabs"
  const val SEARCH_USER_PROFILE = "Search User Profile Screen"
}

object Screen {
  const val OVERVIEW = "Overview Screen"
  const val SEARCH = "Search Screen"
  const val PROFILE = "Profile Screen"
  const val AUTH = "SignIn Screen"
  const val ADD_TRIP = "Add Trip Screen"
  const val EDIT_PROFILE = "Edit Profile Screen"
  const val ADD_ACTIVITY = "Add Activity Screen"
  const val TOP_TABS = "Top Tabs Screen"
    const val ACTIVITIES_FOR_ONE_DAY = "Activities For One Day Screen"
  const val SEARCH_USER_PROFILE = "Search User Profile Screen"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val textId: String,
)

object TopLevelDestinations {
  val OVERVIEW =
      TopLevelDestination(route = Route.OVERVIEW, icon = Icons.Outlined.Menu, textId = "Overview")
  val SEARCH =
      TopLevelDestination(route = Route.SEARCH, icon = Icons.Outlined.Search, textId = "Search")
  val PROFILE =
      TopLevelDestination(route = Route.PROFILE, icon = Icons.Outlined.Person, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.OVERVIEW, TopLevelDestinations.SEARCH, TopLevelDestinations.PROFILE)

/** State for the navigation of the app */
open class NavigationState {
  /**
   * This is a mutable state that represents the current tab index for the trip. (0 for Schedule, 1
   * for Activities, 2 for Settings) This is used to determine which tab is currently selected in
   * the TobTabs composable for a trip. It needs to be part of the navigation actions in order to
   * remember which tab was selecting when opening another screen and trying to go back. For
   * example, when we open AddActivityScreen from the Activities tab, we want to go back to this
   * tab.
   */
  var currentTabIndexForTrip by mutableIntStateOf(0) // Default to 0 (Schedule tab)
  /**
   * This is a mutable state that represents whether the daily view is selected in the ByDayScreen.
   * This is used to determine which view is currently selected in the Schedule Screen. Similarly to
   * currentTabIndexForTrip, it needs to be part of the navigation actions in order to remember
   * which view was selected when opening another screen and trying to go back.
   */
  var isDailyViewSelected by mutableStateOf(true) // Default to true (Daily view selected)
}

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to Clear the back stack when
   *   navigating to a new destination This is useful when navigating to a new screen from the
   *   bottom navigation bar as we don't want to keep the previous screen in the back stack
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      // Pop up to the start destination of the graph to
      // avoid building up a large stack of destinations
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
        inclusive = true
      }

      // Avoid multiple copies of the same destination when reselecting same item
      launchSingleTop = true

      // Restore state when reselecting a previously selected item
      restoreState = true
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String = navController.currentDestination?.route ?: ""

  private val navigationState = NavigationState()

  open fun getNavigationState(): NavigationState {
    return navigationState
  }
}

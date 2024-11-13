package com.android.voyageur.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
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
  const val BY_DAY = "By Day Screen"
  const val BY_WEEK = "By Week Screen"
  const val ACTIVITIES = "Activities Screen"
  const val ADD_ACTIVITY = "Add Activity Screen"
  const val SETTINGS = "Settings Screen"
  const val TOP_TABS = "Top Tabs Screen"
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
}

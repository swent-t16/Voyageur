package com.android.voyageur

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.authentication.AuthenticationWrapper
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.notifications.AndroidNotificationProvider
import com.android.voyageur.ui.notifications.AndroidStringProvider
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.EditProfileScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.PlaceDetailsScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.search.SearchUserProfileScreen
import com.android.voyageur.ui.trip.AddActivityScreen
import com.android.voyageur.ui.trip.TopTabs
import com.android.voyageur.ui.trip.activities.ActivitiesForOneDayScreen
import com.android.voyageur.ui.trip.activities.EditActivityScreen
import com.android.voyageur.ui.trip.assistant.AssistantScreen
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Composable function that sets up the main navigation and screens for the Voyageur app.
 *
 * The function configures and initializes the navigation controller, sets up the ViewModels for
 * various data sources (Trips, User, Places), and establishes navigation routes for authentication,
 * overview, trip management, user profiles, and activity-related screens.
 *
 * @param placesClient The client used to interact with the Google Places API, providing place data.
 */
@Composable
fun VoyageurApp(placesClient: PlacesClient) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory)
  // Retrieve context from the UI layer
  val context = LocalContext.current

  // Create the providers here
  val stringProvider = AndroidStringProvider(context)
  val notificationProvider = AndroidNotificationProvider(context)

  // Use UserViewModel.provideFactory(context) instead of UserViewModel.Factory
  val userViewModel: UserViewModel =
      viewModel(
          factory =
              UserViewModel.provideFactory(
                  notificationProvider = notificationProvider,
                  stringProvider = stringProvider,
              ))

  val placesViewModel: PlacesViewModel =
      viewModel(factory = PlacesViewModel.provideFactory(placesClient))

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { AuthenticationWrapper(navigationActions) }
    }

    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) {
        OverviewScreen(tripsViewModel, navigationActions, userViewModel)
      }
      composable(Screen.ADD_TRIP) {
        AddTripScreen(
            tripsViewModel,
            navigationActions,
            userViewModel = userViewModel,
            placesViewModel = placesViewModel)
      }
    }

    navigation(
        startDestination = Screen.SEARCH,
        route = Route.SEARCH,
    ) {
      composable(Screen.SEARCH) {
        SearchScreen(userViewModel, placesViewModel, tripsViewModel, navigationActions)
      }
      composable(Screen.SEARCH_USER_PROFILE) {
        SearchUserProfileScreen(userViewModel, navigationActions)
      }
      composable(Screen.PLACE_DETAILS) { PlaceDetailsScreen(navigationActions, placesViewModel) }
    }

    navigation(
        startDestination = Screen.PROFILE,
        route = Route.PROFILE,
    ) {
      composable(Screen.PROFILE) { ProfileScreen(userViewModel, tripsViewModel, navigationActions) }
      composable(Screen.EDIT_PROFILE) {
        EditProfileScreen(userViewModel, navigationActions, tripsViewModel)
      }
    }

    navigation(startDestination = Screen.TOP_TABS, route = Route.TOP_TABS) {
      composable(Screen.TOP_TABS) {
        TopTabs(
            tripsViewModel,
            navigationActions,
            userViewModel = userViewModel,
            placesViewModel = placesViewModel)
      }
      composable(Screen.ADD_ACTIVITY) {
        AddActivityScreen(tripsViewModel, navigationActions, placesViewModel)
      }
      composable(Screen.ACTIVITIES_FOR_ONE_DAY) {
        ActivitiesForOneDayScreen(tripsViewModel, navigationActions)
      }
      composable(Screen.ASSISTANT) {
        AssistantScreen(tripsViewModel, navigationActions, userViewModel)
      }
      composable(Screen.EDIT_ACTIVITY) {
        EditActivityScreen(navigationActions, tripsViewModel, placesViewModel)
      }
    }
  }
}

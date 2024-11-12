package com.android.voyageur

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.authentication.SignInScreen
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.AddTripScreen
import com.android.voyageur.ui.overview.OverviewScreen
import com.android.voyageur.ui.profile.EditProfileScreen
import com.android.voyageur.ui.profile.ProfileScreen
import com.android.voyageur.ui.search.SearchScreen
import com.android.voyageur.ui.search.SearchUserProfileScreen
import com.android.voyageur.ui.trip.AddActivityScreen
import com.android.voyageur.ui.trip.TopTabs
import com.google.android.libraries.places.api.net.PlacesClient

@Composable
fun VoyageurApp(placesClient: PlacesClient) {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    val placesViewModel: PlacesViewModel =
        viewModel(factory = PlacesViewModel.provideFactory(placesClient))

    NavHost(navController = navController, startDestination = Route.AUTH) {
        navigation(
            startDestination = Screen.AUTH,
            route = Route.AUTH,
        ) {
            composable(Screen.AUTH) { SignInScreen(navigationActions) }
        }
        navigation(
            startDestination = Screen.OVERVIEW,
            route = Route.OVERVIEW,
        ) {
            composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigationActions) }
            composable(Screen.ADD_TRIP) { AddTripScreen(tripsViewModel, navigationActions) }
        }
        navigation(
            startDestination = Screen.SEARCH,
            route = Route.SEARCH,
        ) {
            composable(Screen.SEARCH) { SearchScreen(userViewModel, placesViewModel, navigationActions) }
            composable(Screen.SEARCH_USER_PROFILE) {SearchUserProfileScreen(userViewModel, navigationActions) }
        }
        navigation(
            startDestination = Screen.PROFILE,
            route = Route.PROFILE,
        ) {
            composable(Screen.PROFILE) { ProfileScreen(userViewModel, navigationActions) }
            composable(Screen.EDIT_PROFILE) { EditProfileScreen(userViewModel, navigationActions) }
        }

        navigation(startDestination = Screen.TOP_TABS, route = Route.TOP_TABS) {
            composable(Screen.TOP_TABS) { TopTabs(tripsViewModel, navigationActions) }
            composable(Screen.ADD_ACTIVITY) { AddActivityScreen(tripsViewModel, navigationActions) }
        }
    }
}
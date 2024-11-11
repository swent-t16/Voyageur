package com.android.voyageur

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
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
        // Authentication Navigation Graph
        navigation(startDestination = Screen.AUTH, route = "auth_graph") {
            composable(Screen.AUTH) { SignInScreen(navigationActions) }
        }

        // Overview Navigation Graph
        navigation(startDestination = Screen.OVERVIEW, route = "overview_graph") {
            composable(Screen.OVERVIEW) { OverviewScreen(tripsViewModel, navigationActions) }
            composable(Screen.ADD_TRIP) { AddTripScreen(tripsViewModel, navigationActions) }
        }

        // Search Navigation Graph
        navigation(startDestination = Screen.SEARCH, route = "search_graph") {
            composable(Screen.SEARCH) { SearchScreen(userViewModel, placesViewModel, navigationActions) }

            // Search User Profile as a nested screen within Search Graph
            composable(
                route = Route.SEARCH_USER_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                if (userId != null) {
                    SearchUserProfileScreen(
                        userId = userId,
                        userViewModel = userViewModel,
                        navigationActions = navigationActions
                    )
                } else {
                    // Handle null userId by navigating back to Search
                    LaunchedEffect(Unit) {
                        navigationActions.navigateTo(Screen.SEARCH)
                    }
                }
            }
        }

        // Profile Navigation Graph
        navigation(startDestination = Screen.PROFILE, route = "profile_graph") {
            composable(Screen.PROFILE) { ProfileScreen(userViewModel, navigationActions) }
            composable(Screen.EDIT_PROFILE) { EditProfileScreen(userViewModel, navigationActions) }
        }

        // Top Tabs Navigation Graph
        navigation(startDestination = Screen.TOP_TABS, route = "top_tabs_graph") {
            composable(Screen.TOP_TABS) { TopTabs(tripsViewModel, navigationActions) }
            composable(Screen.ADD_ACTIVITY) { AddActivityScreen(tripsViewModel, navigationActions) }
        }
    }
}

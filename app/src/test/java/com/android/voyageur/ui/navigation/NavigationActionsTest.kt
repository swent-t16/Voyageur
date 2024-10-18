package com.android.voyageur.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class NavigationActionsTest {

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockGraph: NavGraph
  private lateinit var mockDestination: NavDestination

  @Before
  fun setUp() {
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
    mockGraph = mock(NavGraph::class.java)
    mockDestination = mock(NavDestination::class.java)

    `when`(navHostController.graph).thenReturn(mockGraph)
    `when`(mockGraph.findStartDestination()).thenReturn(mockDestination)
    `when`(mockDestination.id).thenReturn(1)
  }

  @Test
  fun testNavigateToTopLevelDestination() {
    val destination = TopLevelDestinations.OVERVIEW

    navigationActions.navigateTo(destination)

    verify(navHostController).navigate(eq(destination.route), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun testNavigateToScreen() {
    val screen = "details"

    navigationActions.navigateTo(screen)

    verify(navHostController).navigate(screen)
  }

  @Test
  fun testGoBack() {
    navigationActions.goBack()

    verify(navHostController).popBackStack()
  }

  @Test
  fun testCurrentRoute() {
    val route = "home"
    val mockCurrentDestination = mock(NavDestination::class.java)
    `when`(mockCurrentDestination.route).thenReturn(route)
    `when`(navHostController.currentDestination).thenReturn(mockCurrentDestination)

    val currentRoute = navigationActions.currentRoute()

    assert(currentRoute == route)
  }

  @Test
  fun testCurrentRouteWhenNull() {
    `when`(navHostController.currentDestination).thenReturn(null)

    val currentRoute = navigationActions.currentRoute()

    assert(currentRoute.isEmpty())
  }
}

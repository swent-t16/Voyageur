package com.android.voyageur.ui.trip.schedule

import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripRepository
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

class WeeklyViewScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var tripsViewModel: TripsViewModel
    private lateinit var navigationActions: NavigationActions
    private lateinit var tripRepository: TripRepository
    private lateinit var mockTrip: Trip

    @Before
    fun setUp() {
        tripRepository = mock(TripRepository::class.java)
        navigationActions = mock(NavigationActions::class.java)
        tripsViewModel = TripsViewModel(tripRepository)

        // Create mock trip with activities
        mockTrip = Trip(
            id = "test-trip",
            name = "London Trip",
            startDate = Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 0, 0)
                .toInstant(ZoneOffset.UTC))),
            endDate = Timestamp(Date.from(LocalDateTime.of(2024, 11, 4, 0, 0)
                .toInstant(ZoneOffset.UTC))),
            activities = listOf(
                Activity(
                    "1",
                    "",
                    Location("","","",""),
                    startTime = Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 14, 0)
                        .toInstant(ZoneOffset.UTC))),
                    endDate = Timestamp(Date.from(LocalDateTime.of(2024, 10, 3, 14, 0)
                        .toInstant(ZoneOffset.UTC))),
                    0,
                    ActivityType.MUSEUM
                ),
                Activity(
                    "2",
                    "",
                    Location("","","",""),
                    startTime = Timestamp(Date.from(LocalDateTime.of(2024, 10, 4, 14, 0)
                        .toInstant(ZoneOffset.UTC))),
                    endDate = Timestamp(Date.from(LocalDateTime.of(2024, 10, 4, 14, 0)
                        .toInstant(ZoneOffset.UTC))),
                    0,
                    ActivityType.MUSEUM
                )
            )
        )

        // Set up view model with mock trip
        whenever(tripsViewModel.selectedTrip).thenReturn(MutableStateFlow(mockTrip))
    }

    @Test
    fun weeklyViewScreen_displaysCorrectInitialState() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify basic UI elements
        composeTestRule.onNodeWithText("Daily").assertExists()
        composeTestRule.onNodeWithText("Weekly").assertExists()
        composeTestRule.onNodeWithText("London Trip").assertExists()
    }

    @Test
    fun weeklyViewScreen_displaysCorrectWeekRanges() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify week ranges are displayed
        composeTestRule.onNodeWithText("Oct 3 - Oct 9").assertExists()
        composeTestRule.onNodeWithText("Oct 10 - Oct 16").assertExists()
    }

    @Test
    fun weeklyViewScreen_displaysDaysWithCorrectActivityCounts() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify activity counts
        composeTestRule.onNodeWithText("M 3  -  2 activities").assertExists()
    }

    @Test
    fun dayActivityCount_navigatesToDayView() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Click on a day
        composeTestRule.onNodeWithText("M 3  -  2 activities").performClick()

        // Verify navigation
        verify(navigationActions).navigateTo(Screen.BY_DAY)
        verify(tripsViewModel).selectTrip(mockTrip)
    }

    @Test
    fun weeklyViewScreen_handlesEmptyTrip() {
        val emptyTrip = mockTrip.copy(activities = emptyList())
        whenever(tripsViewModel.selectedTrip).thenReturn(MutableStateFlow(emptyTrip))

        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify days show zero activities
        composeTestRule.onNodeWithText("M 3  -  0 activities").assertExists()
    }

    @Test
    fun weeklyViewScreen_displaysCorrectCardWidth() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify the week card has the correct width
        composeTestRule.onNodeWithText("Oct 3 - Oct 9")
            .assertIsDisplayed()
            .assertWidthIsEqualTo(360.dp)
    }

    @Test
    fun generateWeeks_returnsCorrectNumberOfWeeks() {
        val weeks = generateWeeks(mockTrip.startDate, mockTrip.endDate)
        assert(weeks.size >= 2) // Should contain at least two weeks for the given date range
    }

    @Test
    fun formatDate_returnsCorrectFormat() {
        val startDate = mockTrip.startDate.toDate().toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        val formattedDate = formatDate(startDate)
        assert(formattedDate == "Oct 3")
    }

    @Test
    fun weeklyViewScreen_hasCorrectBottomNavigation() {
        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify bottom navigation exists
        composeTestRule.onNodeWithTag("bottomNavigationMenu").assertExists()
    }

    @Test
    fun weeklyViewScreen_handlesNullTrip() {
        whenever(tripsViewModel.selectedTrip).thenReturn(MutableStateFlow(null))

        composeTestRule.setContent {
            WeeklyViewScreen(tripsViewModel, navigationActions)
        }

        // Verify error message is shown
        composeTestRule.onNodeWithText("No ToDo selected. Should not happen").assertExists()
    }
}
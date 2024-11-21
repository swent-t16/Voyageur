package com.android.voyageur.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesRepository
import com.android.voyageur.model.place.PlacesViewModel
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class PlaceSearchWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var placesViewModel: PlacesViewModel
    private lateinit var placesRepository: PlacesRepository


    @Before
    fun setUp() {
        placesRepository = Mockito.mock(PlacesRepository::class.java)
        placesViewModel = PlacesViewModel(placesRepository)
    }

    @Test
    fun testInitialState() {
        `when`(placesRepository.searchPlaces(any(), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[1] as (List<Place>) -> Unit
            onSuccess(emptyList())
        }
        composeTestRule.setContent {
            PlaceSearchWidget(
                placesViewModel = placesViewModel,
                onSelect = {}
            )
        }

        composeTestRule.onNodeWithTag("searchTextField").assertIsDisplayed()
    }

    @Test
    fun testSearch() {
        val place = CustomPlace(Place.builder().setName("Test Place").build(), emptyList())
        val searchedPlaces = MutableStateFlow(listOf(place))
        `when`(placesRepository.searchPlaces(any(), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[1] as (List<CustomPlace>) -> Unit
            onSuccess(searchedPlaces.value)
        }
        composeTestRule.setContent {
            PlaceSearchWidget(
                placesViewModel = placesViewModel,
                onSelect = {}
            )
        }

        composeTestRule.onNodeWithTag("searchTextField").performTextInput("Test")
        composeTestRule.onNodeWithTag("searchDropdown").assertIsDisplayed()
    }

}

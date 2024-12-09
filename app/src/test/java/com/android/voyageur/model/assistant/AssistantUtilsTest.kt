package com.android.voyageur.model.assistant

import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePromptTest {

  @Test
  fun testDatePromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("start date year 2024 month 7 day 1"))
    assertTrue(prompt.contains("end date year 2024 month 7 day 7"))
  }

  @Test
  fun testInterestsPromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = listOf("hiking", "cycling"),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(
        prompt.contains(
            "The activities should focus on the following interests (if applicable): hiking, cycling."))
  }

  @Test
  fun testEmptyInterestsIsNotGenerated() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertFalse(
        prompt.contains("The activities should focus on the following interests (if applicable):"))
  }

  @Test
  fun testEnumTypePromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("The activity type can only be"))
  }

  @Test
  fun testAlreadyPresentActivitiesPromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = listOf("activity1", "activity2"))

    assertTrue(
        prompt.contains(
            "The following activities are already present in the trip: activity1, activity2. Please avoid them."))
  }

  @Test
  fun testEmptyAlreadyPresentActivitiesIsNotGenerated() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertFalse(prompt.contains("The following activities are already present in the trip:"))
  }

  @Test
  fun testDraftActivitiesPromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("List a lot of popular specific activities"))
  }

  @Test
  fun testFinalActivitiesPromptGeneratesCorrectly() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = true,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("Make a full schedule by listing specific activities"))
  }

  @Test
  fun testTripDetailsAreIncluded() {
    val trip =
        Trip(
            name = "Trip Name",
            description = "Trip Description",
            location = Location(id = "", name = "Location Name"),
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("The trip, called Trip Name"))
    assertTrue(prompt.contains("description Trip Description"))
    assertTrue(prompt.contains("location Location Name"))
  }

  @Test
  fun testUserPromptIsIncluded() {
    val trip =
        Trip(
            name = "Trip Name",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val prompt =
        generatePrompt(
            trip = trip,
            userPrompt = "User Prompt",
            interests = emptyList(),
            provideFinalActivities = false,
            alreadyPresentActivities = emptyList())

    assertTrue(prompt.contains("with the following prompt: User Prompt"))
  }

  // Helper function to create a Timestamp from year, month, and day
  private fun createTimestamp(year: Int, month: Int, day: Int): Timestamp {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day, 0, 0, 0) // Month is 0-indexed
    return Timestamp(calendar.time)
  }
}

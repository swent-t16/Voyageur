package com.android.voyageur.model.assistant

import com.android.voyageur.model.trip.Trip
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePromptTest {

  @Test
  fun testGeneratePromptForFinalActivitiesWithoutInterests() {
    val trip =
        Trip(
            name = "Summer Adventure",
            startDate = createTimestamp(2024, 7, 1),
            endDate = createTimestamp(2024, 7, 7))

    val userPrompt = "Explore beaches and try local cuisine"
    val provideFinalActivities = true
    val interests = listOf("hiking", "cycling")
    val useInterests = false

    val prompt = generatePrompt(trip, userPrompt, interests, provideFinalActivities, useInterests)
    println(prompt)

    assertTrue(prompt.contains("Summer Adventure"))
    assertTrue(prompt.contains("start date year 2024 month 7 day 1"))
    assertTrue(prompt.contains("end date year 2024 month 7 day 7"))
    assertTrue(prompt.contains("Make a full schedule by listing activities"))
    assertTrue(prompt.contains("Explore beaches and try local cuisine"))
    assertFalse(prompt.contains("hiking"))
    assertFalse(prompt.contains("cycling"))
  }

  @Test
  fun testGeneratePromptForDraftActivitiesWithoutInterests() {
    val trip =
        Trip(
            name = "Winter Wonderland",
            startDate = createTimestamp(2024, 12, 20),
            endDate = createTimestamp(2024, 12, 27))

    val userPrompt = "Enjoy snowy landscapes and Christmas markets"
    val provideFinalActivities = false
    val interests = listOf("hiking, cycling")
    val useInterests = false

    val prompt = generatePrompt(trip, userPrompt, interests, provideFinalActivities, useInterests)

    assertTrue(prompt.contains("Winter Wonderland"))
    assertTrue(prompt.contains("start date year 2024 month 12 day 20"))
    assertTrue(prompt.contains("end date year 2024 month 12 day 27"))
    assertTrue(prompt.contains("List a lot of popular specific activities"))
    assertTrue(prompt.contains("Enjoy snowy landscapes and Christmas markets"))
    assertFalse(prompt.contains("hiking"))
    assertFalse(prompt.contains("cycling"))
  }

  @Test
  fun testGeneratePromptWithInterests() {
    val trip =
        Trip(
            name = "Spring Break",
            startDate = createTimestamp(2024, 3, 1),
            endDate = createTimestamp(2024, 3, 7))

    val userPrompt = "Relax and have fun"
    val provideFinalActivities = false
    val interests = listOf("hiking", "cycling")
    val useInterests = true

    val prompt = generatePrompt(trip, userPrompt, interests, provideFinalActivities, useInterests)

    assertTrue(prompt.contains("Spring Break"))
    assertTrue(prompt.contains("start date year 2024 month 3 day 1"))
    assertTrue(prompt.contains("end date year 2024 month 3 day 7"))
    assertTrue(prompt.contains("List a lot of popular specific activities"))
    assertTrue(prompt.contains("Relax and have fun"))
    assertTrue(prompt.contains("hiking"))
    assertTrue(prompt.contains("cycling"))
  }

  @Test
  fun testGeneratePromptWithInterestsAndFinalActivities() {
    val trip =
        Trip(
            name = "Spring Break",
            startDate = createTimestamp(2024, 3, 1),
            endDate = createTimestamp(2024, 3, 7))

    val userPrompt = "Relax and have fun"
    val provideFinalActivities = true
    val interests = listOf("hiking", "cycling")
    val useInterests = true

    val prompt = generatePrompt(trip, userPrompt, interests, provideFinalActivities, useInterests)

    assertTrue(prompt.contains("Spring Break"))
    assertTrue(prompt.contains("start date year 2024 month 3 day 1"))
    assertTrue(prompt.contains("end date year 2024 month 3 day 7"))
    assertTrue(prompt.contains("Make a full schedule by listing activities"))
    assertTrue(prompt.contains("Relax and have fun"))
    assertTrue(prompt.contains("hiking"))
    assertTrue(prompt.contains("cycling"))
  }

  // Helper function to create a Timestamp from year, month, and day
  private fun createTimestamp(year: Int, month: Int, day: Int): Timestamp {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day, 0, 0, 0) // Month is 0-indexed
    return Timestamp(calendar.time)
  }
}

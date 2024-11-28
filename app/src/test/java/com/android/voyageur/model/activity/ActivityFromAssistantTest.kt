package com.android.voyageur.model.activity

import com.android.voyageur.model.assistant.ActivityFromAssistant
import com.android.voyageur.model.assistant.convertActivityFromAssistantToActivity
import com.android.voyageur.model.assistant.extractActivitiesFromAssistantFromJson
import com.android.voyageur.model.assistant.getYearMonthDay
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityFromAssistantTest {

  @Test
  fun testExtractActivitiesFromAssistantFromJson() {
    val jsonString =
        """
            [
                {
                    "title": "Hiking",
                    "description": "A scenic mountain hike.",
                    "year": 2024,
                    "month": 5,
                    "day": 20,
                    "startTimeHour": 9,
                    "startTimeMinute": 30,
                    "endTimeHour": 15,
                    "endTimeMinute": 0,
                    "estimatedPrice": 50.0,
                    "activityType": "OUTDOORS"
                },
                {
                    "title": "Dinner",
                    "description": "Dinner at a fancy restaurant.",
                    "year": 2024,
                    "month": 5,
                    "day": 20,
                    "startTimeHour": 19,
                    "startTimeMinute": 0,
                    "endTimeHour": 21,
                    "endTimeMinute": 30,
                    "estimatedPrice": 150.0,
                    "activityType": "WALK"
                }
            ]
        """

    val activities = extractActivitiesFromAssistantFromJson(jsonString)
    assertEquals(2, activities.size)

    val firstActivity = activities[0]
    assertEquals("Hiking", firstActivity.title)
    assertEquals(2024, firstActivity.year)
    assertEquals(5, firstActivity.month)
    assertEquals(50.0, firstActivity.estimatedPrice, 0.0)
  }

  @Test
  fun testConvertActivityFromAssistantToActivity() {
    val assistantActivity =
        ActivityFromAssistant(
            title = "Cycling",
            description = "Cycling around the city.",
            year = 2024,
            month = 6,
            day = 15,
            startTimeHour = 10,
            startTimeMinute = 0,
            endTimeHour = 12,
            endTimeMinute = 0,
            estimatedPrice = 20.0,
            activityType = "OUTDOORS")

    val convertedActivity = convertActivityFromAssistantToActivity(assistantActivity)

    assertEquals("Cycling", convertedActivity.title)
    assertEquals("Cycling around the city.", convertedActivity.description)
    assertEquals(ActivityType.OUTDOORS, convertedActivity.activityType)

    val startCalendar = Calendar.getInstance().apply { time = convertedActivity.startTime.toDate() }
    assertEquals(2024, startCalendar.get(Calendar.YEAR))
    assertEquals(6, startCalendar.get(Calendar.MONTH) + 1) // Months are 0-indexed
    assertEquals(15, startCalendar.get(Calendar.DAY_OF_MONTH))
  }

  @Test
  fun testInvalidActivityType() {
    val assistantActivity =
        ActivityFromAssistant(
            title = "Mystery Event",
            description = "An unknown activity type.",
            year = 2024,
            month = 12,
            day = 25,
            startTimeHour = 14,
            startTimeMinute = 0,
            endTimeHour = 16,
            endTimeMinute = 0,
            estimatedPrice = 100.0,
            activityType = "UNKNOWN_TYPE")

    val convertedActivity = convertActivityFromAssistantToActivity(assistantActivity)
    assertEquals(ActivityType.OTHER, convertedActivity.activityType)
  }

  @Test
  fun testGetYearMonthDay() {
    val timestamp = Timestamp(1735689600, 0) // January 1, 2025
    val (year, month, day) = getYearMonthDay(timestamp)

    assertEquals(2025, year)
    assertEquals(0, month) // January
    assertEquals(1, day)
  }
}

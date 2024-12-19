package com.android.voyageur.model.assistant

import android.util.Log
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.time.ZoneId

/**
 * We need a separate class for the JSON that the assistant generates since the assistant can
 * confuse the TimeStamp format (it's less error prone to generate dates and times in a readable .
 * This class is used to store the data in a format that the assistant can understand.
 */
data class ActivityFromAssistant(
    val title: String = "",
    val description: String = "",
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val startTimeHour: Int = 0,
    val startTimeMinute: Int = 0,
    val endTimeHour: Int = 0,
    val endTimeMinute: Int = 0,
    val estimatedPrice: Double = 0.0,
    val activityType: String = "" // it's a string because the generativeModel can't generate enums
)

/**
 * Extracts activities that the assistant generated from a JSON string.
 *
 * @param jsonString the JSON string
 * @return the list of activities
 */
fun extractActivitiesFromAssistantFromJson(jsonString: String): MutableList<ActivityFromAssistant> {
  val gson = Gson()
  val activityListType = object : TypeToken<List<ActivityFromAssistant>>() {}.type
  return try {
    gson.fromJson(jsonString, activityListType)
  } catch (e: JsonSyntaxException) {
    Log.e("ActivityFromAssistant", "Error parsing JSON: ${e.message}")
    mutableListOf() // Return an empty list if parsing fails
  } catch (e: Exception) {
    Log.e("ActivityFromAssistant", "Unexpected error: ${e.message}")
    mutableListOf() // Handle any other unexpected exceptions
  }
}

/**
 * Converts an activity from the assistant to an activity that can be used in the app.
 *
 * @param activityFromAssistant the activity from the assistant
 * @return the activity that can be used in the app
 */
fun convertActivityFromAssistantToActivity(activityFromAssistant: ActivityFromAssistant): Activity {
  return Activity(
      title = activityFromAssistant.title,
      description = activityFromAssistant.description,
      startTime =
          Timestamp(
              java.util.Date.from(
                  java.time.LocalDateTime.of(
                          activityFromAssistant.year,
                          activityFromAssistant.month,
                          activityFromAssistant.day,
                          activityFromAssistant.startTimeHour,
                          activityFromAssistant.startTimeMinute)
                      .atZone(ZoneId.systemDefault())
                      .toInstant())),
      endTime =
          Timestamp(
              java.util.Date.from(
                  java.time.LocalDateTime.of(
                          activityFromAssistant.year,
                          activityFromAssistant.month,
                          activityFromAssistant.day,
                          activityFromAssistant.endTimeHour,
                          activityFromAssistant.endTimeMinute)
                      .atZone(ZoneId.systemDefault())
                      .toInstant())),
      estimatedPrice = activityFromAssistant.estimatedPrice,
      activityType =
          try {
            ActivityType.valueOf(activityFromAssistant.activityType)
          } catch (e: IllegalArgumentException) {
            Log.e(
                "ActivityFromAssistant",
                "Activity type not found: ${activityFromAssistant.activityType}")
            ActivityType.OTHER
          })
}

/**
 * Extracts the year, month, and day from a Firebase `Timestamp` and returns them as a Triple.
 *
 * @param timestamp The `Timestamp` object representing a specific point in time.
 * @return A `Triple` containing the year, month (0-based, where January is 0), and day of the month.
 *
 */
fun getYearMonthDay(timestamp: Timestamp): Triple<Int, Int, Int> {
  val date = timestamp.toDate()
  val calendar = java.util.Calendar.getInstance()
  calendar.time = date
  return Triple(
      calendar.get(java.util.Calendar.YEAR),
      calendar.get(java.util.Calendar.MONTH),
      calendar.get(java.util.Calendar.DAY_OF_MONTH))
}

package com.android.voyageur.model.activity

import android.util.Log
import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.ZoneId
import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val title: String = "",
    val description: String = "",
    val location: Location = Location(""),
    val startTime: Timestamp = Timestamp(0, 0),
    val endTime: Timestamp = Timestamp(0, 0),
    val estimatedPrice: Double = 0.0,
    val activityType: ActivityType = ActivityType.WALK,
)

enum class ActivityType {
  WALK,
  RESTAURANT,
  MUSEUM,
  SPORTS,
  OUTDOORS,
  TRANSPORT,
  OTHER,
}

/**
 * We need a separate class for the JSON that the assistant generates since the assistant can
 * confuse the TimeStamp format. This class is used to store the data in a format that the assistant
 * can understand.
 */
data class ActivityForAssistant(
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
    val activityType: String = ""
)

fun Activity.hasDescription() = description != ""

fun Activity.hasStartTime() = startTime != Timestamp(0, 0)

fun Activity.hasEndDate() = endTime != Timestamp(0, 0)

fun Activity.isDraft() = !(hasStartTime() && hasEndDate())

fun extractActivitiesFromJson(jsonString: String): MutableList<Activity> {
  val gson = Gson()
  val activityListType = object : TypeToken<List<Activity>>() {}.type
  return gson.fromJson(jsonString, activityListType)
}

fun extractActivitiesForAssistantFromJson(jsonString: String): MutableList<ActivityForAssistant> {
  val gson = Gson()
  val activityListType = object : TypeToken<List<ActivityForAssistant>>() {}.type
  return gson.fromJson(jsonString, activityListType)
}

fun convertActivityForAssistantToActivity(activityForAssistant: ActivityForAssistant): Activity {
  return Activity(
      title = activityForAssistant.title,
      description = activityForAssistant.description,
      startTime =
          Timestamp(
              java.util.Date.from(
                  java.time.LocalDateTime.of(
                          activityForAssistant.year,
                          activityForAssistant.month,
                          activityForAssistant.day,
                          activityForAssistant.startTimeHour,
                          activityForAssistant.startTimeMinute)
                      .atZone(ZoneId.systemDefault())
                      .toInstant())),
      endTime =
          Timestamp(
              java.util.Date.from(
                  java.time.LocalDateTime.of(
                          activityForAssistant.year,
                          activityForAssistant.month,
                          activityForAssistant.day,
                          activityForAssistant.endTimeHour,
                          activityForAssistant.endTimeMinute)
                      .atZone(ZoneId.systemDefault())
                      .toInstant())),
      estimatedPrice = activityForAssistant.estimatedPrice,
      activityType =
          try {
            ActivityType.valueOf(activityForAssistant.activityType)
          } catch (e: IllegalArgumentException) {
            Log.e("GeminiTest", "Activity type not found: ${activityForAssistant.activityType}")
            ActivityType.OTHER
          })
}

fun getYearMonthDay(timestamp: Timestamp): Triple<Int, Int, Int> {
  val date = timestamp.toDate()
  val calendar = java.util.Calendar.getInstance()
  calendar.time = date
  return Triple(
      calendar.get(java.util.Calendar.YEAR),
      calendar.get(java.util.Calendar.MONTH),
      calendar.get(java.util.Calendar.DAY_OF_MONTH))
}

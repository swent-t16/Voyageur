package com.android.voyageur.model.assistant

import android.util.Log
import com.android.voyageur.BuildConfig
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig

/**
 * The generative model that is used to generate activities for a trip. It corresponds to the
 * ActivityFromAssistant class.
 */
val generativeModel =
    GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig =
            generationConfig {
              responseMimeType = "application/json"
              responseSchema =
                  Schema(
                      name = "activities",
                      description = "List of activities",
                      type = FunctionType.ARRAY,
                      items =
                          Schema(
                              name = "activity",
                              description = "An activity",
                              type = FunctionType.OBJECT,
                              properties =
                                  mapOf(
                                      "title" to
                                          Schema(
                                              name = "title",
                                              description = "Title of the activity",
                                              type = FunctionType.STRING),
                                      "description" to
                                          Schema(
                                              name = "description",
                                              description = "Description of the activity",
                                              type = FunctionType.STRING),
                                      "year" to
                                          Schema(
                                              name = "year",
                                              description = "Year of the activity",
                                              type = FunctionType.INTEGER),
                                      "month" to
                                          Schema(
                                              name = "month",
                                              description = "Month of the activity",
                                              type = FunctionType.INTEGER),
                                      "day" to
                                          Schema(
                                              name = "day",
                                              description = "Day of the activity",
                                              type = FunctionType.INTEGER),
                                      "startTimeHour" to
                                          Schema(
                                              name = "startTimeHour",
                                              description =
                                                  "Start time hour of the activity. Must be between 0 and 23",
                                              type = FunctionType.INTEGER),
                                      "startTimeMinute" to
                                          Schema(
                                              name = "startTimeMinute",
                                              description =
                                                  "Start time minute of the activity. Must be between 0 and 59",
                                              type = FunctionType.INTEGER),
                                      "endTimeHour" to
                                          Schema(
                                              name = "endTimeHour",
                                              description =
                                                  "End time hour of the activity. Must be between 0 and 23",
                                              type = FunctionType.INTEGER),
                                      "endTimeMinute" to
                                          Schema(
                                              name = "endTimeMinute",
                                              description =
                                                  "End time minute of the activity. Must be between 0 and 59",
                                              type = FunctionType.INTEGER),
                                      "estimatedPrice" to
                                          Schema(
                                              name = "estimatedPrice",
                                              description = "Estimated price of the activity",
                                              type = FunctionType.NUMBER),
                                      "activityType" to
                                          Schema(
                                              name = "activityType",
                                              description =
                                                  "Type of the activity. can only be WALK, RESTAURANT, MUSEUM, SPORTS, OUTDOORS, TRANSPORT, OTHER",
                                              type = FunctionType.STRING),
                                  ),
                              required =
                                  listOf(
                                      "title",
                                      "description",
                                      "year",
                                      "month",
                                      "day",
                                      "startTimeHour",
                                      "startTimeMinute",
                                      "endTimeHour",
                                      "endTimeMinute",
                                      "estimatedPrice",
                                      "activityType")))
            })

/**
 * The prompt that is used to generate activities for a trip.
 *
 * @param trip the trip
 * @param userPrompt the prompt that the user provides in the app
 * @param interests the interests to focus on
 * @param provideFinalActivities whether to provide final activities with date and time or just
 *   draft activities. In the case of draft activities, the prompt is a bit different to avoid
 *   recommending a lunch for each day more or less the same.
 *     @param alreadyPresentActivities the activities that are already present in the trip
 * @return the prompt to send to use with the generative model
 */
fun generatePrompt(
    trip: Trip,
    userPrompt: String,
    interests: List<String>,
    provideFinalActivities: Boolean,
    alreadyPresentActivities: List<String>
): String {
    val possibleEnumTypePrompt = "The activity type can only be ${ActivityType.entries.joinToString { ", " }}"
    Log.d("AssistantUtils", "possibleEnumTypePrompt: $possibleEnumTypePrompt")
  val startDate = getYearMonthDay(trip.startDate)
  val endDate = getYearMonthDay(trip.endDate)
  val datePrompt =
      """
          between the start date year ${startDate.first} month ${startDate.second + 1} day ${startDate.third} 
          and the end date year ${endDate.first} month ${endDate.second + 1} day ${endDate.third}
            """
          .trimIndent()
  val interestsPrompt =
      if (interests.isNotEmpty()) {
        "The activities should focus on the following interests (if applicable): ${interests.joinToString(", ")}."
      } else {
        ""
      }
  Log.d("AssistantUtils", "interestsPrompt: $interestsPrompt")
  val alreadyPresentActivitiesPrompt =
      if (alreadyPresentActivities.isNotEmpty()) {
        "The following activities are already present in the trip: ${alreadyPresentActivities.joinToString(", ")}. Please avoid them."
      } else {
        ""
      }
  Log.d("AssistantUtils", "alreadyPresentActivitiesPrompt: $alreadyPresentActivitiesPrompt")
  val prompt =
      if (provideFinalActivities) {
        """
    Make a full schedule by listing activities, including separate activities for eating, transport, etc.
    The trip, called ${trip.name}, takes place $datePrompt with the following prompt: $userPrompt. $interestsPrompt $alreadyPresentActivitiesPrompt $possibleEnumTypePrompt
    Recommend multiple activities for each day.
    """
            .trimIndent()
      } else {
        """
    List a lot of popular specific activities to do on a trip called ${trip.name}.
    The trip takes place $datePrompt with the following prompt: $userPrompt. $interestsPrompt $alreadyPresentActivitiesPrompt $possibleEnumTypePrompt
    """
            .trimIndent()
      }

  return prompt
}

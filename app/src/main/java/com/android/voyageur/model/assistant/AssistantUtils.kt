package com.android.voyageur.model.assistant

import android.content.Context
import com.android.voyageur.BuildConfig
import com.android.voyageur.R
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.Trip
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig

/**
 * The generative model that is used to generate activities for a trip. It corresponds to the
 * ActivityFromAssistant class. It contains the schema for the response of the generative model.
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
 * @param context the context. It is used to get the string resources.
 * @param trip the trip
 * @param userPrompt the prompt that the user provides in the app
 * @param interests the interests to focus on
 * @param provideFinalActivities whether to provide final activities with date and time or just
 *   draft activities. In the case of draft activities, the prompt is a bit different to avoid
 *   recommending a lunch for each day more or less the same.
 * @param alreadyPresentActivities the activities that are already present in the trip
 * @return the prompt to send to use with the generative model
 */
fun generatePrompt(
    context: Context,
    trip: Trip,
    userPrompt: String,
    interests: List<String>,
    provideFinalActivities: Boolean,
    alreadyPresentActivities: List<String>
): String {

  // specifies which enum types are possible
  val possibleEnumTypePrompt =
      context.getString(R.string.possible_enum_type_prompt, ActivityType.entries.joinToString(", "))

  val startDate = getYearMonthDay(trip.startDate)
  val endDate = getYearMonthDay(trip.endDate)
  // specifies the date range of the trip
  val datePrompt =
      context.getString(
          R.string.date_prompt,
          startDate.first,
          startDate.second + 1,
          startDate.third,
          endDate.first,
          endDate.second + 1,
          endDate.third)

  // specifies the interests of the user (if non-empty)
  val interestsPrompt =
      if (interests.isNotEmpty()) {
        context.getString(R.string.interests_prompt, interests.joinToString(", "))
      } else {
        ""
      }

  // specifies the titles of the activities that are already present in the trip
  val alreadyPresentActivitiesPrompt =
      if (alreadyPresentActivities.isNotEmpty()) {
        context.getString(
            R.string.already_present_activities_prompt, alreadyPresentActivities.joinToString(", "))
      } else {
        ""
      }

  // specifies whether the prompt is for recommending draft or final activities
  val draftVsFinalPrompt =
      if (provideFinalActivities) {
        context.getString(R.string.draft_vs_final_prompt_final)
      } else {
        context.getString(R.string.draft_vs_final_prompt_draft)
      }

  // combines all the prompts into a single prompt and mentions the trip name, description, type and
  // location
  val prompt =
      context.getString(
          R.string.full_trip_prompt,
          draftVsFinalPrompt,
          trip.name,
          trip.description,
          trip.type,
          trip.location.name,
          datePrompt,
          userPrompt,
          interestsPrompt,
          alreadyPresentActivitiesPrompt,
          possibleEnumTypePrompt)
  return prompt
}

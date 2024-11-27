package com.android.voyageur.model.assistant

import com.android.voyageur.BuildConfig
import com.android.voyageur.model.activity.getYearMonthDay
import com.android.voyageur.model.trip.Trip
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.generationConfig

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
                                              description = "Start time hour of the activity",
                                              type = FunctionType.INTEGER),
                                      "startTimeMinute" to
                                          Schema(
                                              name = "startTimeMinute",
                                              description = "Start time minute of the activity",
                                              type = FunctionType.INTEGER),
                                      "endTimeHour" to
                                          Schema(
                                              name = "endTimeHour",
                                              description = "End time hour of the activity",
                                              type = FunctionType.INTEGER),
                                      "endTimeMinute" to
                                          Schema(
                                              name = "endTimeMinute",
                                              description = "End time minute of the activity",
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

fun generatePrompt(trip: Trip, userPrompt: String) : String {
    val startDate = getYearMonthDay(trip.startDate)
    val endDate = getYearMonthDay(trip.endDate)
    val datePrompt =
        "between the start date year ${startDate.first} month ${startDate.second + 1} day ${startDate.third} and the end date year ${endDate.first} month ${endDate.second + 1} day ${endDate.third}"
   val prompt = "Make a full schedule by listing activities including separate activities for each separate activity, e.g. eating, transport, basically i want suggestions for every day spent on a trip called ${trip.name} that takes place $datePrompt" +
                    " and with the following prompt: $userPrompt. You should recommend multiple activities for each day."
      return prompt
}

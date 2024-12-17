package com.android.voyageur.ui.trip

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.components.PlaceSearchWidget
import com.android.voyageur.ui.formFields.DatePickerModal
import com.android.voyageur.ui.formFields.TimePickerInput
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A Composable function for creating or editing an activity within a trip.
 *
 * This screen allows users to input details about an activity, such as title, description,
 * location, date, time, estimated price, and activity type. It validates inputs and saves the
 * activity to the selected trip when all criteria are met.
 *
 * @param tripsViewModel The [TripsViewModel] instance to manage trip data and updates.
 * @param navigationActions A set of actions to handle navigation between screens.
 * @param placesViewModel The [PlacesViewModel] instance to manage location-related data and
 *   queries.
 * @param existingActivity An optional [Activity] to edit; if null, a new activity will be created.
 *
 * ## Features:
 * - **Input Fields:** Users can enter a title, description, location, date, and time for the
 *   activity.
 * - **Date and Time Selection:** Includes a [DatePickerModal] for selecting a date and a
 *   [TimePickerInput] dialog for specifying start and end times.
 * - **Validation:** Ensures the selected date is within the trip's range, end time is after start
 *   time, and required fields like the title are filled.
 * - **Integration with Places API:** Provides location suggestions and lets users select from
 *   search results.
 * - **Dynamic UI Updates:** Automatically updates UI with new or edited activity details.
 * - **Save Functionality:** Saves or updates the activity in the trip's activities list via
 *   [TripsViewModel].
 *
 * ## Error Handling:
 * Displays toast messages for validation errors, such as:
 * - Missing or invalid date and time inputs.
 * - Dates outside the trip's range.
 * - End time earlier than start time.
 *
 * ## Navigation:
 * - The screen navigates back to the previous screen after successfully saving the activity.
 * - Uses [NavigationActions] for navigation commands.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddActivityScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    placesViewModel: PlacesViewModel,
    existingActivity: Activity? = null
) {
  var title by remember { mutableStateOf(existingActivity?.title ?: "") }
  var description by remember { mutableStateOf(existingActivity?.description ?: "") }
  var query by remember { mutableStateOf(TextFieldValue(existingActivity?.location?.name ?: "")) }
  var selectedLocation by remember { mutableStateOf<Location>(Location("", "", "", 0.0, 0.0)) }
  var showModal by remember { mutableStateOf(false) }
  var activityDate by remember {
    mutableStateOf<Long?>(existingActivity?.startTime?.toDate()?.time.takeIf { it != 0L })
  }
  var showTime by remember { mutableStateOf(false) }
  var selectedTimeField by remember { mutableStateOf<TimeField?>(null) }
  var startTime by remember {
    mutableStateOf<Long?>(
        if (existingActivity?.startTime?.toDate()?.time != 0L && activityDate != null)
            existingActivity?.startTime?.toDate()?.time
        else null)
  }
  var endTime by remember {
    mutableStateOf<Long?>(
        if (existingActivity?.endTime?.toDate()?.time != 0L && activityDate != null)
            existingActivity?.endTime?.toDate()?.time
        else null)
  }

  var priceInput by remember { mutableStateOf(existingActivity?.estimatedPrice?.toString() ?: "") }
  var estimatedPrice by remember {
    mutableStateOf<Double?>(existingActivity?.estimatedPrice ?: 0.0)
  }
  var activityType by remember {
    mutableStateOf(existingActivity?.activityType ?: ActivityType.WALK)
  }
  var expanded by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val formattedDate = activityDate?.let { dateFormat.format(Date(it)) } ?: ""

  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  var formattedStartTime = startTime?.let { timeFormat.format(Date(it)) } ?: ""
  var formattedEndTime =
      if (endTime != null && activityDate != null) timeFormat.format(Date(endTime!!)) else ""

  var isSaving by remember { mutableStateOf(false) }

  val keyboardController = LocalSoftwareKeyboardController.current

  fun createActivity() {
    if (isSaving) return // Prevent duplicate saves

    fun normalizeToMidnight(date: Date): Date {
      val calendar =
          Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
      return calendar.time
    }

    val dateNormalized = activityDate?.let { normalizeToMidnight(Date(it)) } ?: Date(0)

    val selectedTrip = tripsViewModel.selectedTrip.value!!
    val startDateNormalized = normalizeToMidnight(selectedTrip.startDate.toDate())
    val endDateNormalized = normalizeToMidnight(selectedTrip.endDate.toDate())

    val startTimestamp =
        Timestamp(
            startTime?.let {
              Calendar.getInstance()
                  .apply {
                    time = dateNormalized
                    set(
                        Calendar.HOUR_OF_DAY,
                        Calendar.getInstance()
                            .apply { timeInMillis = it }
                            .get(Calendar.HOUR_OF_DAY))
                    set(
                        Calendar.MINUTE,
                        Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.MINUTE))
                  }
                  .time
            }
                ?: dateNormalized.apply {
                  Calendar.getInstance()
                      .apply {
                        time = dateNormalized
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                      }
                      .time
                })

    val endTimestamp =
        Timestamp(
            endTime?.let {
              Calendar.getInstance()
                  .apply {
                    time = dateNormalized
                    set(
                        Calendar.HOUR_OF_DAY,
                        Calendar.getInstance()
                            .apply { timeInMillis = it }
                            .get(Calendar.HOUR_OF_DAY))
                    set(
                        Calendar.MINUTE,
                        Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.MINUTE))
                  }
                  .time
            }
                ?: Calendar.getInstance()
                    .apply {
                      time = dateNormalized
                      set(Calendar.HOUR_OF_DAY, 23)
                      set(Calendar.MINUTE, 59)
                      set(Calendar.SECOND, 59)
                      set(Calendar.MILLISECOND, 999)
                    }
                    .time)

    when {
      activityDate == null && (startTime != null || endTime != null) -> {
        Toast.makeText(
                context, context.getString(R.string.select_activity_date), Toast.LENGTH_SHORT)
            .show()
        return
      }
      activityDate != null &&
          (dateNormalized.after(endDateNormalized) ||
              dateNormalized.before(startDateNormalized)) -> {
        Toast.makeText(
                context, context.getString(R.string.date_between_trip_dates), Toast.LENGTH_SHORT)
            .show()
        return
      }
      startTime == null && endTime != null -> {
        Toast.makeText(context, context.getString(R.string.select_start_time), Toast.LENGTH_SHORT)
            .show()
        return
      }
      startTime != null &&
          endTime != null &&
          endTimestamp.toDate().before(startTimestamp.toDate()) -> {
        Toast.makeText(
                context, context.getString(R.string.end_time_after_start), Toast.LENGTH_SHORT)
            .show()
        return
      }
      activityDate != null && startTime == null && endTime == null -> {
        Toast.makeText(context, context.getString(R.string.default_times), Toast.LENGTH_SHORT)
            .show()
      }
      activityDate == null -> {
        Toast.makeText(context, context.getString(R.string.created_draft), Toast.LENGTH_SHORT)
            .show()
      }
      startTime != null && endTime == null -> {
        Toast.makeText(context, context.getString(R.string.default_end_time), Toast.LENGTH_SHORT)
            .show()
      }
    }

    val activity =
        Activity(
            title = title,
            description = description,
            location = selectedLocation,
            startTime = startTimestamp,
            endTime = endTimestamp,
            estimatedPrice = estimatedPrice ?: 0.0,
            activityType = activityType)

    val updatedActivities =
        if (existingActivity != null) {
          selectedTrip.activities.map { if (it == existingActivity) activity else it }
        } else {
          selectedTrip.activities + activity
        }

    val updatedTrip = selectedTrip.copy(activities = updatedActivities)
    isSaving = true
    tripsViewModel.updateTrip(
        updatedTrip,
        onSuccess = {
          isSaving = false
          /*
              This is a trick to force a recompose, because the reference wouldn't
              change and update the UI.
          */
          tripsViewModel.selectTrip(Trip())
          tripsViewModel.selectTrip(updatedTrip)
          navigationActions.goBack()
        },
        onFailure = { error ->
          isSaving = false
          Toast.makeText(
                  context,
                  context.getString(R.string.fail_add_activity, error.message),
                  Toast.LENGTH_SHORT)
              .show()
          Log.e(
              "AddActivityScreen",
              context.getString(R.string.error_add_activity, error.message),
              error)
        })
  }

  Scaffold(
      modifier = Modifier.testTag("addActivity"),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  if (existingActivity == null) stringResource(R.string.create_activity)
                  else stringResource(R.string.edit_activity),
                  Modifier.testTag("addActivityTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                  }
            })
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          Column(
              modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    isError = title.isEmpty(),
                    label = { Text(stringResource(R.string.activity_title)) },
                    placeholder = { Text(stringResource(R.string.name_activity)) },
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityTitle"),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    singleLine = true)

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.activity_description)) },
                    placeholder = { Text(stringResource(R.string.describe_activity)) },
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityDescription"))

                Spacer(modifier = Modifier.height(2.dp))

                PlaceSearchWidget(
                    placesViewModel = placesViewModel,
                    onSelect = { place ->
                      selectedLocation = place
                      query = TextFieldValue(place.name)
                    },
                    query = query,
                    onQueryChange = { it, location ->
                      placesViewModel.setQuery(it.text, location)
                      query = it
                    })

                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.activity_date)) },
                    colors =
                        TextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface),
                    modifier =
                        Modifier.fillMaxWidth().testTag("inputDate").clickable { showModal = true })

                if (showModal) {
                  DatePickerModal(
                      onDateSelected = { selectedDate ->
                        activityDate = selectedDate
                        showModal = false
                      },
                      onDismiss = { showModal = false },
                      selectedDate = activityDate ?: System.currentTimeMillis())
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  OutlinedTextField(
                      value = formattedStartTime,
                      onValueChange = {},
                      readOnly = true,
                      enabled = false,
                      label = { Text(stringResource(R.string.start_time)) },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = MaterialTheme.colorScheme.onSurface),
                      modifier =
                          Modifier.fillMaxWidth(0.49f).testTag("inputStartTime").clickable {
                            selectedTimeField = TimeField.START
                            showTime = true
                          })

                  OutlinedTextField(
                      value = formattedEndTime,
                      onValueChange = {},
                      readOnly = true,
                      enabled = false,
                      label = { Text(stringResource(R.string.end_time)) },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = MaterialTheme.colorScheme.onSurface),
                      modifier =
                          Modifier.fillMaxWidth(0.49f).testTag("inputEndTime").clickable {
                            selectedTimeField = TimeField.END
                            showTime = true
                          })
                }

                if (showTime) {
                  Dialog(onDismissRequest = { showTime = false }) {
                    TimePickerInput(
                        initialHour =
                            when (selectedTimeField) {
                              TimeField.START ->
                                  startTime?.let {
                                    Calendar.getInstance()
                                        .apply { timeInMillis = it }
                                        .get(Calendar.HOUR_OF_DAY)
                                  }
                              TimeField.END ->
                                  endTime?.let {
                                    Calendar.getInstance()
                                        .apply { timeInMillis = it }
                                        .get(Calendar.HOUR_OF_DAY)
                                  }
                              else -> null
                            },
                        initialMinute =
                            when (selectedTimeField) {
                              TimeField.START ->
                                  startTime?.let {
                                    Calendar.getInstance()
                                        .apply { timeInMillis = it }
                                        .get(Calendar.MINUTE)
                                  }
                              TimeField.END ->
                                  endTime?.let {
                                    Calendar.getInstance()
                                        .apply { timeInMillis = it }
                                        .get(Calendar.MINUTE)
                                  }
                              else -> null
                            },
                        onTimeSelected = { selectedTime ->
                          if (selectedTime != null) {
                            if (selectedTimeField == TimeField.START) {
                              startTime = selectedTime
                              formattedStartTime = timeFormat.format(Date(startTime!!))
                            } else {
                              endTime = selectedTime
                              formattedEndTime = timeFormat.format(Date(endTime!!))
                            }
                          }
                          showTime = false
                        },
                        onDismiss = { showTime = false })
                  }
                }

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { input ->
                      val filteredInput = input.filter { it.isDigit() || it == '.' }
                      if (filteredInput.count { it == '.' } <= 1) {
                        val decimalParts = filteredInput.split(".")
                        if (decimalParts.size <= 1 || decimalParts[1].length <= 2) {
                          priceInput = filteredInput
                          estimatedPrice = filteredInput.toDoubleOrNull()
                        }
                      }
                    },
                    label = { Text(stringResource(R.string.estimated_price_CHF)) },
                    placeholder = { Text(stringResource(R.string.enter_estimated_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityPrice"))

                Spacer(modifier = Modifier.height(2.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.testTag("activityTypeDropdown")) {
                      TextField(
                          value = activityType.name.lowercase().replaceFirstChar { it.uppercase() },
                          onValueChange = {},
                          readOnly = true,
                          label = { Text(stringResource(R.string.select_activity_type)) },
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                          },
                          modifier =
                              Modifier.fillMaxWidth().menuAnchor().testTag("inputActivityType"))
                      ExposedDropdownMenu(
                          expanded = expanded,
                          onDismissRequest = { expanded = false },
                          modifier = Modifier.testTag("expandedDropdown")) {
                            ActivityType.entries.forEach { type ->
                              DropdownMenuItem(
                                  text = {
                                    Text(
                                        text =
                                            type.name.lowercase().replaceFirstChar {
                                              it.uppercase()
                                            })
                                  },
                                  onClick = {
                                    activityType = type
                                    expanded = false
                                  })
                            }
                          }
                    }

                Spacer(modifier = Modifier.height(16.dp))
              }

          Button(
              onClick = { createActivity() },
              enabled = title.isNotBlank() && !isSaving,
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("activitySave")) {
                Text(stringResource(R.string.save))
              }
        }
      }
}

enum class TimeField {
  START,
  END
}

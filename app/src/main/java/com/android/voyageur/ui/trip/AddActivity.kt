package com.android.voyageur.ui.trip

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddActivityScreen(tripsViewModel: TripsViewModel, navigationActions: NavigationActions) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var location by remember { mutableStateOf("") }
  var showModal by remember { mutableStateOf(false) }
  var activityDate by remember { mutableStateOf<Long?>(null) }
  var showTime by remember { mutableStateOf(false) }
  var selectedTimeField by remember { mutableStateOf<TimeField?>(null) }
  var startTime by remember { mutableStateOf<Long?>(null) }
  var endTime by remember { mutableStateOf<Long?>(null) }
  var estimatedPrice by remember { mutableStateOf<Double>(0.0) }
  var activityType by remember { mutableStateOf(ActivityType.WALK) }
  var expanded by remember { mutableStateOf(false) }

  val context = LocalContext.current

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val formattedDate = activityDate?.let { dateFormat.format(Date(it)) } ?: ""

  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  var formattedStartTime = startTime?.let { timeFormat.format(Date(it)) } ?: ""
  var formattedEndTime = endTime?.let { timeFormat.format(Date(it)) } ?: ""

  fun createActivity() {
    if (activityDate == null && (startTime != null || endTime != null)) {
      Toast.makeText(context, "Please select an activity date first", Toast.LENGTH_SHORT).show()
      return
    }

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

    val today = normalizeToMidnight(Date())
    val dateNormalized = normalizeToMidnight(Date(activityDate!!))

    if (dateNormalized.before(today)) {
      Toast.makeText(context, "The activity date cannot be in the past", Toast.LENGTH_SHORT).show()
      return
    }

    val startTimestamp =
        startTime
            ?.let {
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
            ?.let { Timestamp(it) } ?: Timestamp(Date())

    val endTimestamp =
        endTime
            ?.let {
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
            ?.let { Timestamp(it) } ?: Timestamp(Date())

    if (endTimestamp.toDate().before(startTimestamp.toDate())) {
      Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
      return
    }

    val activity =
        Activity(
            title = title,
            description = description,
            location = Location(country = "Unknown", city = "Unknown", county = null, zip = null),
            startTime = startTimestamp,
            endTime = endTimestamp,
            estimatedPrice = estimatedPrice,
            activityType = activityType)

    val selectedTrip = tripsViewModel.selectedTrip.value!!
    val updatedTrip = selectedTrip.copy(activities = selectedTrip.activities + activity)
    tripsViewModel.updateTrip(
        updatedTrip,
        onSuccess = {
          /*
              This is a trick to force a recompose, because the reference wouldn't
              change and update the UI.
          */
          tripsViewModel.selectTrip(Trip())
          tripsViewModel.selectTrip(updatedTrip)
          navigationActions.goBack()
        })
  }

  Scaffold(
      modifier = Modifier.testTag("addActivity"),
      topBar = {
        TopAppBar(
            title = { Text("Create a New Activity", Modifier.testTag("addActivityTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
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
                    label = { Text("Title *") },
                    placeholder = { Text("Name the activity") },
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityTitle"))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the activity") },
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityDescription"))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("Enter the location of the activity") },
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityLocation"))

                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date") },
                    colors =
                        TextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = Color.Black),
                    modifier =
                        Modifier.fillMaxWidth().testTag("inputDate").clickable { showModal = true })

                if (showModal) {
                  DatePickerModal(
                      onDateSelected = { selectedDate ->
                        activityDate = selectedDate
                        showModal = false
                      },
                      onDismiss = { showModal = false })
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
                      label = { Text("Start Time") },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = Color.Black),
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
                      label = { Text("End Time") },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = Color.Black),
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
                    value = estimatedPrice.toString(),
                    onValueChange = { input ->
                      val newValue = input.replace("[^0-9.]".toRegex(), "")
                      estimatedPrice = newValue.toDouble()
                    },
                    label = { Text("Estimated Price (CHF)") },
                    placeholder = { Text("Enter estimated price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("inputActivityPrice"))

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                      TextField(
                          value = activityType.name,
                          onValueChange = {},
                          readOnly = true,
                          label = { Text("Select Activity Type") },
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                          },
                          modifier =
                              Modifier.fillMaxWidth().menuAnchor().testTag("inputActivityType"))
                      ExposedDropdownMenu(
                          expanded = expanded, onDismissRequest = { expanded = false }) {
                            ActivityType.entries.forEach { type ->
                              DropdownMenuItem(
                                  text = { Text(text = type.name) },
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
              enabled = title.isNotBlank(),
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("activitySave")) {
                Text("Save")
              }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val datePickerState = rememberDatePickerState()

  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = {
              onDateSelected(datePickerState.selectedDateMillis)
              onDismiss()
            }) {
              Text("OK")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        DatePicker(state = datePickerState)
      }
}

enum class TimeField {
  START,
  END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerInput(
    initialHour: Int?,
    initialMinute: Int?,
    onTimeSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
  val timeState = rememberTimePickerState(is24Hour = true)

  if (initialHour != null && initialMinute != null) {
    timeState.hour = initialHour
    timeState.minute = initialMinute
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 10.dp,
        modifier = Modifier.padding(16.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                text = "Select time",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium)
            TimeInput(state = timeState)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = onDismiss) { Text(text = "Cancel") }
              TextButton(
                  onClick = {
                    val selectedTime =
                        Calendar.getInstance()
                            .apply {
                              set(Calendar.HOUR_OF_DAY, timeState.hour)
                              set(Calendar.MINUTE, timeState.minute)
                            }
                            .timeInMillis
                    onTimeSelected(selectedTime)
                  }) {
                    Text(text = "OK")
                  }
            }
          }
        }
  }
}

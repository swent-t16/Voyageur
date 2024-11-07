package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTripScreen(
    tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory),
    navigationActions: NavigationActions,
    isEditMode: Boolean = false
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var locations by remember { mutableStateOf("") }
  var showModal by remember { mutableStateOf(false) }
  var selectedDateField by remember { mutableStateOf<DateField?>(null) }
  var startDate by remember { mutableStateOf<Long?>(null) }
  var endDate by remember { mutableStateOf<Long?>(null) }
  var tripType by remember { mutableStateOf(TripType.BUSINESS) }
  var imageUri by remember { mutableStateOf("") }

  val context = LocalContext.current
  val imageId = R.drawable.default_trip_image

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val formattedStartDate = startDate?.let { dateFormat.format(Date(it)) } ?: ""
  val formattedEndDate = endDate?.let { dateFormat.format(Date(it)) } ?: ""

  val disabledBorderColor = Color.DarkGray
  val disabledTextColor = Color.Black

  val galleryLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri -> uri?.let { imageUri = it.toString() } })

  LaunchedEffect(isEditMode) {
    if (isEditMode && tripsViewModel.selectedTrip.value != null) {
      tripsViewModel.selectedTrip.value?.let { trip ->
        name = trip.name
        description = trip.description
        tripType = trip.type
        imageUri = trip.imageUri
        startDate = trip.startDate.toDate().time
        endDate = trip.endDate.toDate().time
      }
    }
  }

  fun createTripWithImage(imageUrl: String) {

    if (startDate == null || endDate == null) {
      Toast.makeText(context, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
      return
    }

    val startTimestamp = Timestamp(Date(startDate!!))
    val endTimestamp = Timestamp(Date(endDate!!))

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
    val startDateNormalized = normalizeToMidnight(Date(startDate!!))
    val endDateNormalized = normalizeToMidnight(Date(endDate!!))

    if (startDateNormalized.before(today) || endDateNormalized.before(today)) {
      Toast.makeText(context, "Start and end dates cannot be in the past", Toast.LENGTH_SHORT)
          .show()
      return
    }
    if (startDateNormalized.after(endDateNormalized)) {
      Toast.makeText(context, "End date cannot be before start date", Toast.LENGTH_SHORT).show()
      return
    }

    val trip =
        Trip(
            id =
                if (isEditMode) tripsViewModel.selectedTrip.value!!.id
                else tripsViewModel.getNewTripId(),
            creator = Firebase.auth.uid.orEmpty(),
            description = description,
            name = name,
            locations =
                locations.split(";").map { locationString ->
                  val parts = locationString.split(",").map { it.trim() }
                  if (parts.size >= 2) {
                    Location(
                        country = parts[0],
                        city = parts[1],
                        county = parts.getOrNull(2),
                        zip = parts.getOrNull(3))
                  } else {
                    Location(country = "Unknown", city = "Unknown", county = null, zip = null)
                  }
                },
            startDate = startTimestamp,
            endDate = endTimestamp,
            activities = listOf(),
            type = tripType,
            imageUri = imageUrl)
    if (!isEditMode) tripsViewModel.createTrip(trip, onSuccess = { navigationActions.goBack() })
    else {
      tripsViewModel.updateTrip(trip, onSuccess = { navigationActions.goBack() })
    }
  }

  Scaffold(
      modifier = Modifier.testTag("addTrip"),
      topBar = {
        if (!isEditMode)
            TopAppBar(
                title = { Text("Create a New Trip", Modifier.testTag("addTripTitle")) },
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
                if (imageUri != "") {
                  Image(
                      painter = rememberAsyncImagePainter(model = imageUri),
                      contentDescription = "Selected image",
                      contentScale = ContentScale.Crop,
                      modifier =
                          Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(5.dp)))
                } else {
                  Image(
                      painter = painterResource(id = imageId),
                      contentDescription = "Default trip image",
                      contentScale = ContentScale.Crop,
                      modifier =
                          Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(5.dp)))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()) {
                      Text("Select Image from Gallery")
                    }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    isError = name.isEmpty(),
                    label = { Text("Trip *") },
                    placeholder = { Text("Name the trip") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripTitle"))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the trip") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripDescription"))

                OutlinedTextField(
                    value = locations,
                    onValueChange = { locations = it },
                    label = { Text("Locations") },
                    placeholder = { Text("Name the locations, comma-separated") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripLocations"))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  OutlinedTextField(
                      value = formattedStartDate,
                      onValueChange = {},
                      readOnly = true,
                      enabled = false,
                      label = { Text("Start Date *") },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              // focusedTextColor = Color.Black,
                              // unfocusedTextColor = Color.Black,
                              disabledTextColor = Color.Black),
                      modifier =
                          Modifier.fillMaxWidth(0.49f).testTag("inputStartDate").clickable {
                            selectedDateField = DateField.START
                            showModal = true
                          })

                  OutlinedTextField(
                      value = formattedEndDate,
                      onValueChange = {},
                      readOnly = true,
                      enabled = false,
                      label = { Text("End Date *") },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              // focusedTextColor = Color.Black,
                              // unfocusedTextColor = Color.Black,
                              disabledTextColor = Color.Black),
                      modifier =
                          Modifier.fillMaxWidth(0.49f).testTag("inputEndDate").clickable {
                            selectedDateField = DateField.END
                            showModal = true
                          })
                }
                if (showModal) {
                  DatePickerModal(
                      onDateSelected = { selectedDate ->
                        if (selectedDateField == DateField.START) {
                          startDate = selectedDate
                        } else if (selectedDateField == DateField.END) {
                          endDate = selectedDate
                        }
                        showModal = false
                      },
                      onDismiss = { showModal = false },
                      selectedDate =
                          when (selectedDateField) {
                            DateField.START -> startDate ?: System.currentTimeMillis()
                            DateField.END -> endDate ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                          })
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                      RadioButton(
                          onClick = { tripType = TripType.BUSINESS },
                          selected = tripType == TripType.BUSINESS,
                          modifier = Modifier.testTag("tripTypeBusiness"))
                      Text("Business")
                      RadioButton(
                          onClick = { tripType = TripType.TOURISM },
                          selected = tripType == TripType.TOURISM,
                          modifier = Modifier.testTag("tripTypeTourism"))
                      Text("Tourism")
                    }

                Spacer(modifier = Modifier.height(16.dp))
              }

          Button(
              onClick = {
                if (imageUri.isNotBlank() &&
                    imageUri != tripsViewModel.selectedTrip.value?.imageUri) {
                  val imageUriParsed = Uri.parse(imageUri)
                  tripsViewModel.uploadImageToFirebase(
                      uri = imageUriParsed,
                      onSuccess = { downloadUrl -> createTripWithImage(downloadUrl) },
                      onFailure = { exception ->
                        Toast.makeText(
                                context,
                                "Failed to upload image: ${exception.message}",
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                } else {
                  createTripWithImage(imageUri)
                }
              },
              enabled =
                  name.isNotBlank() &&
                      formattedStartDate.isNotBlank() &&
                      formattedEndDate.isNotBlank(),
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("tripSave")) {
                Text("Save Trip")
              }
        }
      }
}

enum class DateField {
  START,
  END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    selectedDate: Long = System.currentTimeMillis()
) {

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

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

package com.android.voyageur.ui.overview

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.android.voyageur.ui.formFields.DatePickerModal
import com.android.voyageur.ui.gallery.PermissionButtonForGallery
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.utils.rememberImageCropper
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
    isEditMode: Boolean = false,
    onUpdate: () -> Unit = {}
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
  var showRationaleDialog by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val imageId = R.drawable.default_trip_image

  // Get screen dimensions and calculate responsive image height
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val imageHeight = remember(screenWidth) { (screenWidth * 9f / 16f).coerceAtMost(300.dp) }

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val formattedStartDate = startDate?.let { dateFormat.format(Date(it)) } ?: ""
  val formattedEndDate = endDate?.let { dateFormat.format(Date(it)) } ?: ""

  // Use the new image cropper utility
  val imageCropper = rememberImageCropper { result ->
    result.imageUri?.let { imageUri = it }
    result.error?.let { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
  }
  val permissionVersion =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        READ_MEDIA_IMAGES
      } else {
        READ_EXTERNAL_STORAGE
      }

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
            activities =
                if (isEditMode) tripsViewModel.selectedTrip.value?.activities ?: listOf()
                else listOf(),
            type = tripType,
            imageUri = imageUrl)

    if (!isEditMode) {
      tripsViewModel.createTrip(trip)
      navigationActions.goBack()
    } else {
      tripsViewModel.updateTrip(
          trip,
          onSuccess = {
            tripsViewModel.selectTrip(Trip())
            tripsViewModel.selectTrip(trip)
            onUpdate()
          })
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
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(imageHeight)
                            .aspectRatio(1.8f)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("imageContainer")) {
                      if (imageUri.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      } else {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = "Default trip image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      }
                    }

                Spacer(modifier = Modifier.height(16.dp))
                PermissionButtonForGallery(
                    onUriSelected = { uri -> imageUri = uri.toString(), imageCropper(null) },
                    "Select Image from Gallery",
                    "This app needs access to your photos to allow you to select an image for your trip.",
                    Modifier.fillMaxWidth())

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
                          ),
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
                          ),
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

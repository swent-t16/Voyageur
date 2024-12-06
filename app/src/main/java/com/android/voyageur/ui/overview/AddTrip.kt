package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.components.PlaceSearchWidget
import com.android.voyageur.ui.formFields.DatePickerModal
import com.android.voyageur.ui.formFields.UserDropdown
import com.android.voyageur.ui.gallery.PermissionButtonForGallery
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A composable function for adding or editing a trip in the travel planner app.
 *
 * This screen allows users to create or update a trip by filling out necessary details such as the
 * trip name, description, participants, location, start and end dates, trip type, and an optional
 * image. It also validates user inputs to ensure logical and meaningful trip data is provided.
 *
 * @param tripsViewModel The [TripsViewModel] instance to manage trip-related data and operations.
 * @param navigationActions The [NavigationActions] instance to handle navigation actions.
 * @param isEditMode A boolean indicating whether the screen is in edit mode or create mode. Default
 *   is `false`.
 * @param onUpdate A callback invoked after successfully updating a trip. Default is an empty
 *   lambda.
 * @param userViewModel The [UserViewModel] instance for managing user-related data.
 * @param placesViewModel The [PlacesViewModel] instance for managing location search functionality.
 *
 * ## Features:
 * - **Create or Edit Trips**: Allows users to input trip details like name, description,
 *   participants, dates, and image.
 * - **User-Friendly Validation**: Ensures valid dates and logical trip information (e.g., start
 *   date before end date).
 * - **Responsive UI**: Adapts to different screen sizes, providing a seamless user experience.
 * - **Gallery Integration**: Lets users select an image from their gallery for the trip.
 * - **Real-Time Feedback**: Displays success or error messages for trip creation or update actions.
 *
 * ## UI Components:
 * - **Text Fields**: For trip name, description, and location search.
 * - **Date Pickers**: To select trip start and end dates.
 * - **User Dropdown**: To select participants from the user's contacts.
 * - **Gallery Button**: For selecting or replacing the trip's image.
 * - **Image Display**: Shows the selected or default trip image.
 *
 * ## Behavior:
 * - **Validation**: Ensures start and end dates are not in the past and are logically ordered.
 * - **Error Handling**: Displays appropriate messages for invalid inputs or operation failures.
 * - **State Management**: Maintains form state during user interaction.
 */
@SuppressLint("StateFlowValueCalledInComposition", "UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTripScreen(
    tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory),
    navigationActions: NavigationActions,
    isEditMode: Boolean = false,
    onUpdate: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    placesViewModel: PlacesViewModel
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var query by remember { mutableStateOf(TextFieldValue("")) }
  var selectedLocation by remember { mutableStateOf<Location>(Location("", "", "", 0.0, 0.0)) }
  var showModal by remember { mutableStateOf(false) }
  var selectedDateField by remember { mutableStateOf<DateField?>(null) }
  var startDate by remember { mutableStateOf<Long?>(null) }
  var endDate by remember { mutableStateOf<Long?>(null) }
  var tripType by remember { mutableStateOf(TripType.BUSINESS) }
  var imageUri by remember { mutableStateOf("") }
  var discoverable by remember { mutableStateOf(false) }
  val contactsAndUsers by userViewModel.contacts.collectAsState()
  val userList =
      remember(contactsAndUsers, isEditMode) {
        contactsAndUsers
            .filter { user -> user.id != Firebase.auth.uid.orEmpty() }
            .map {
              if (!isEditMode) Pair(it, false)
              else {
                Pair(it, tripsViewModel.selectedTrip.value?.participants?.contains(it.id) ?: false)
              }
            }
            .toMutableStateList()
      }
  val context = LocalContext.current
  val imageId = R.drawable.default_trip_image

  // Get screen dimensions and calculate responsive image height
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val imageHeight = remember(screenWidth) { (screenWidth * 9f / 16f).coerceAtMost(300.dp) }

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val formattedStartDate = startDate?.let { dateFormat.format(Date(it)) } ?: ""
  val formattedEndDate = endDate?.let { dateFormat.format(Date(it)) } ?: ""

  var isSaving by remember { mutableStateOf(false) }

  val keyboardController = LocalSoftwareKeyboardController.current
  Log.d("USERLIST", contactsAndUsers.size.toString())

  LaunchedEffect(isEditMode) {
    if (isEditMode && tripsViewModel.selectedTrip.value != null) {
      tripsViewModel.selectedTrip.value?.let { trip ->
        name = trip.name
        description = trip.description
        tripType = trip.type
        imageUri = trip.imageUri
        query = TextFieldValue(trip.location.name)
        startDate = trip.startDate.toDate().time
        endDate = trip.endDate.toDate().time
        discoverable = trip.discoverable
      }
    } else {
      //      userList.clear()
    }
  }

  fun createTripWithImage(imageUrl: String) {
    if (isSaving) return // Prevent duplicate saves

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

    if (!isEditMode && (startDateNormalized.before(today) || endDateNormalized.before(today))) {
      Toast.makeText(context, "Start and end dates cannot be in the past", Toast.LENGTH_SHORT)
          .show()
      return
    }
    // if the trip is ongoing then the start date is in the past already so we check just for the
    // end date
    if (isEditMode && endDateNormalized.before(today)) {
      Toast.makeText(context, "End date cannot be in the past", Toast.LENGTH_SHORT).show()
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
            participants =
                (userList.filter { it.second }.map { it.first.id } + Firebase.auth.uid.orEmpty())
                    .toSet()
                    .toList(),
            location = selectedLocation,
            startDate = startTimestamp,
            endDate = endTimestamp,
            activities =
                if (isEditMode) tripsViewModel.selectedTrip.value?.activities ?: listOf()
                else listOf(),
            type = tripType,
            imageUri = imageUrl,
            photos =
                if (isEditMode) tripsViewModel.selectedTrip.value?.photos ?: listOf() else listOf(),
            discoverable = discoverable)

    if (!isEditMode) {
      isSaving = true
      tripsViewModel.createTrip(
          trip,
          onSuccess = {
            isSaving = false
            Toast.makeText(context, "Trip created successfully!", Toast.LENGTH_SHORT).show()
          },
          onFailure = { error ->
            isSaving = false
            Toast.makeText(context, "Failed to create trip: ${error.message}", Toast.LENGTH_SHORT)
                .show()
            Log.e("AddTripScreen", "Error creating trip: ${error.message}", error)
          })
      navigationActions.goBack()
    } else {
      isSaving = true
      tripsViewModel.updateTrip(
          trip,
          onSuccess = {
            isSaving = false
            Toast.makeText(context, "Trip updated successfully!", Toast.LENGTH_SHORT).show()
            /*
                This is a trick to force a recompose, because the reference wouldn't
                change and update the UI.
            */
            tripsViewModel.selectTrip(Trip())
            tripsViewModel.selectTrip(trip)
            onUpdate()
          },
          onFailure = { error ->
            isSaving = false
            Toast.makeText(context, "Failed to update trip: ${error.message}", Toast.LENGTH_SHORT)
                .show()
            Log.e("AddTripScreen", "Error updating trip: ${error.message}", error)
          })
    }
  }

  Scaffold(
      modifier = Modifier.testTag("addTrip"),
      topBar = {
        if (!isEditMode)
            TopAppBar(
                title = {
                  Text(stringResource(R.string.create_trip), Modifier.testTag("addTripTitle"))
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
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(imageHeight)
                            .aspectRatio(1.78f)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("imageContainer")) {
                      if (imageUri.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = stringResource(R.string.selected_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      } else {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = stringResource(R.string.default_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      }
                    }

                Spacer(modifier = Modifier.height(16.dp))
                PermissionButtonForGallery(
                    onUriSelected = { uri -> imageUri = uri.toString() },
                    stringResource(R.string.select_image_gallery),
                    stringResource(R.string.access_gallery),
                    16,
                    9,
                    Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    isError = name.isEmpty(),
                    label = { Text(stringResource(R.string.trip_title)) },
                    placeholder = { Text(stringResource(R.string.name_trip)) },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripTitle"),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    singleLine = true)

                Spacer(modifier = Modifier.height(2.dp))

                UserDropdown(
                    userList,
                    onUpdate = { pair, index -> userList[index] = Pair(pair.first, !pair.second) })

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.trip_description)) },
                    placeholder = { Text(stringResource(R.string.describe_trip)) },
                    modifier = Modifier.fillMaxWidth().testTag("inputTripDescription"))

                Spacer(modifier = Modifier.height(2.dp))

                PlaceSearchWidget(
                    placesViewModel = placesViewModel,
                    onSelect = { location ->
                      selectedLocation = location
                      query = TextFieldValue(location.name)
                    },
                    query = query,
                    onQueryChange = {
                      placesViewModel.setQuery(it.text, null)
                      query = it
                    })

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  OutlinedTextField(
                      value = formattedStartDate,
                      onValueChange = {},
                      readOnly = true,
                      enabled = false,
                      label = { Text(stringResource(R.string.start_date)) },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = MaterialTheme.colorScheme.onSurface),
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
                      label = { Text(stringResource(R.string.end_date)) },
                      colors =
                          TextFieldDefaults.colors(
                              disabledContainerColor = Color.Transparent,
                              disabledTextColor = MaterialTheme.colorScheme.onSurface),
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          modifier = Modifier.padding(end = 16.dp)) {
                            RadioButton(
                                onClick = { tripType = TripType.BUSINESS },
                                selected = tripType == TripType.BUSINESS,
                                modifier = Modifier.testTag("tripTypeBusiness"))
                            Text(
                                stringResource(R.string.business),
                                modifier = Modifier.padding(start = 2.dp))
                          }
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          modifier = Modifier.padding(start = 16.dp)) {
                            RadioButton(
                                onClick = { tripType = TripType.TOURISM },
                                selected = tripType == TripType.TOURISM,
                                modifier = Modifier.testTag("tripTypeTourism"))
                            Text(
                                stringResource(R.string.tourism),
                                modifier = Modifier.padding(start = 2.dp))
                          }
                    }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          stringResource(R.string.make_trip_public),
                          modifier = Modifier.padding(end = 16.dp))
                      Checkbox(
                          checked = discoverable,
                          onCheckedChange = { discoverable = it },
                          modifier = Modifier.testTag("discoverableCheckbox"))
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
                      formattedEndDate.isNotBlank() &&
                      !isSaving,
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("tripSave")) {
                Text(stringResource(R.string.save_trip))
              }
        }
      }
}
// enum for the two different dates: start and end
enum class DateField {
  START,
  END
}

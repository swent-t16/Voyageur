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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.android.voyageur.ui.notifications.AndroidNotificationProvider
import com.android.voyageur.ui.notifications.AndroidStringProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    userViewModel: UserViewModel? = null,
    placesViewModel: PlacesViewModel
) {
  val context = LocalContext.current

  // Create providers here in the UI layer
  val stringProvider = AndroidStringProvider(context)
  val notificationProvider = AndroidNotificationProvider(context)

  // If userViewModel is not provided from above, create it here:
  val actualUserViewModel =
      userViewModel
          ?: viewModel(
              factory =
                  UserViewModel.provideFactory(
                      stringProvider = stringProvider, notificationProvider = notificationProvider))
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var query by remember { mutableStateOf(TextFieldValue("")) }
  var selectedLocation by remember { mutableStateOf<Location>(Location("", "", "", 0.0, 0.0)) }
  var showModal by remember { mutableStateOf(false) }
  var selectedDateField by remember { mutableStateOf<DateField?>(null) }
  var startDate by remember { mutableStateOf<Long?>(null) }
  var endDate by remember { mutableStateOf<Long?>(null) }
  var tripType by remember { mutableStateOf(TripType.TOURISM) }
  var expanded by remember { mutableStateOf(false) }
  var imageUri by remember { mutableStateOf("") }
  var discoverable by remember { mutableStateOf(false) }
  val contactsAndUsers by actualUserViewModel.contacts.collectAsState()
  LaunchedEffect(Unit) { tripsViewModel.fetchTripInvites() }
  val userList =
      remember(tripsViewModel.selectedTrip, contactsAndUsers, isEditMode) {
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
    }
  }

  fun createTripWithImage(imageUrl: String, tripId: String) {
    if (isSaving) return // Prevent duplicate saves

    if (startDate == null || endDate == null) {
      Toast.makeText(context, context.getString(R.string.select_both_dates), Toast.LENGTH_SHORT)
          .show()
      return
    }

    val startTimestamp = Timestamp(Date(startDate!!))
    val endTimestamp = Timestamp(Date(endDate!!))
    fun normalizeToMidnight(date: Date): Date {
      val calendar =
          Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
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
      Toast.makeText(
              context, context.getString(R.string.dates_must_not_be_in_past), Toast.LENGTH_SHORT)
          .show()
      return
    }
    // if the trip is ongoing then the start date is in the past already so we check just for the
    // end date
    if (isEditMode && endDateNormalized.before(today)) {
      Toast.makeText(context, context.getString(R.string.end_date_not_in_past), Toast.LENGTH_SHORT)
          .show()
      return
    }
    if (startDateNormalized.after(endDateNormalized)) {
      Toast.makeText(context, context.getString(R.string.end_after_start), Toast.LENGTH_SHORT)
          .show()
      return
    }

    val trip =
        Trip(
            id = tripId,
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
            Toast.makeText(
                    context, context.getString(R.string.trip_created_success), Toast.LENGTH_SHORT)
                .show()
          },
          onFailure = { error ->
            isSaving = false
            Toast.makeText(
                    context,
                    context.getString(R.string.trip_create_failure, error.message),
                    Toast.LENGTH_SHORT)
                .show()
            Log.e(
                "AddTripScreen",
                context.getString(R.string.trip_create_error, error.message),
                error)
          })
      navigationActions.goBack()
    } else {
      isSaving = true
      tripsViewModel.updateTrip(
          trip,
          onSuccess = {
            isSaving = false
            Toast.makeText(
                    context, context.getString(R.string.trip_updated_success), Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(
                    context,
                    context.getString(R.string.trip_updated_failure, error.message),
                    Toast.LENGTH_SHORT)
                .show()
            Log.e(
                "AddTripScreen",
                context.getString(R.string.trip_updated_error, error.message),
                error)
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
        val tripId =
            if (isEditMode) {
              tripsViewModel.selectedTrip.value!!.id
            } else {
              tripsViewModel.getNewTripId()
            }
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
                if (isEditMode) {
                  UserDropdown(
                      userList,
                      tripsViewModel,
                      tripId,
                      // set to false to remove the user
                      onRemove = { pair, index -> userList[index] = Pair(pair.first, false) })
                }

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
                    onQueryChange = { it, location ->
                      placesViewModel.setQuery(it.text, location)
                      query = it
                    },
                )

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

                Spacer(modifier = Modifier.height(2.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.testTag("tripTypeDropdown")) {
                      TextField(
                          value = tripType.name.lowercase().replaceFirstChar { it.uppercase() },
                          onValueChange = {},
                          readOnly = true,
                          label = { Text(stringResource(R.string.select_trip_type)) },
                          trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                          },
                          modifier = Modifier.fillMaxWidth().menuAnchor().testTag("inputTripType"))
                      ExposedDropdownMenu(
                          expanded = expanded,
                          onDismissRequest = { expanded = false },
                          modifier = Modifier.testTag("expandedDropdownTrips")) {
                            TripType.entries.forEach { type ->
                              DropdownMenuItem(
                                  text = {
                                    Text(
                                        text =
                                            type.name.lowercase().replaceFirstChar {
                                              it.uppercase()
                                            })
                                  },
                                  onClick = {
                                    tripType = type
                                    expanded = false
                                  })
                            }
                          }
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Checkbox(
                          checked = discoverable,
                          onCheckedChange = { discoverable = it },
                          modifier = Modifier.testTag("discoverableCheckbox"))
                      Text(
                          stringResource(R.string.make_trip_public),
                          modifier = Modifier.padding(end = 16.dp))
                    }

                if (!isEditMode) {
                  Row(
                      modifier =
                          Modifier.fillMaxWidth().padding(top = 4.dp).testTag("informativeText"),
                      verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription =
                                null, // Decorative icon, no content description needed
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text =
                                stringResource(
                                    R.string.add_participants_text_when_not_in_edit_mode),
                            color = MaterialTheme.colorScheme.onBackground)
                      }
                }
              }

          Button(
              onClick = {
                if (imageUri.isNotBlank() &&
                    imageUri != tripsViewModel.selectedTrip.value?.imageUri) {
                  val imageUriParsed = Uri.parse(imageUri)
                  tripsViewModel.uploadImageToFirebase(
                      uri = imageUriParsed,
                      onSuccess = { downloadUrl -> createTripWithImage(downloadUrl, tripId) },
                      onFailure = { exception ->
                        Toast.makeText(
                                context,
                                context.getString(R.string.fail_upload_image, exception.message),
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                } else {
                  createTripWithImage(imageUri, tripId)
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

package com.android.voyageur.ui.overview

import android.icu.util.GregorianCalendar
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    tripsViewModel: TripsViewModel = viewModel(factory = TripsViewModel.Factory),
    navigationActions: NavigationActions
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var creator by remember { mutableStateOf("") }
  var participants by remember { mutableStateOf("") }
  var locations by remember { mutableStateOf("") }
  var startDate by remember { mutableStateOf("") }
  var endDate by remember { mutableStateOf("") }
  var tripType by remember { mutableStateOf(TripType.BUSINESS) }

  val context = LocalContext.current

  Scaffold(
      // modifier = Modifier.testTag("addTrip"),
      topBar = {
        TopAppBar(
            title = { Text("Create a New Trip") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back")
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                  value = name,
                  onValueChange = { name = it },
                  label = { Text("Trip") },
                  placeholder = { Text("Name the trip") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  placeholder = { Text("Describe the trip") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = creator,
                  onValueChange = { creator = it },
                  label = { Text("Creator") },
                  placeholder = { Text("Assign a creator") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = participants,
                  onValueChange = { participants = it },
                  label = { Text("Participants") },
                  placeholder = { Text("Name the participants, comma-separated") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = locations,
                  onValueChange = { locations = it },
                  label = { Text("Locations") },
                  placeholder = { Text("Name the locations, comma-separated") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = startDate,
                  onValueChange = { startDate = it },
                  label = { Text("Start Date (DD/MM/YYYY)") },
                  placeholder = { Text("19/01/1975") },
                  modifier = Modifier.fillMaxWidth())

              OutlinedTextField(
                  value = endDate,
                  onValueChange = { endDate = it },
                  label = { Text("End Date (DD/MM/YYYY)") },
                  placeholder = { Text("19/01/1975") },
                  modifier = Modifier.fillMaxWidth())

              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RadioButton(
                        selected = tripType == TripType.BUSINESS,
                        onClick = { tripType = TripType.BUSINESS })
                    Text("Business")
                    RadioButton(
                        selected = tripType == TripType.TOURISM,
                        onClick = { tripType = TripType.TOURISM })
                    Text("Tourism")
                  }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                  onClick = {
                    val calendar = GregorianCalendar()
                    val startParts = startDate.split("/")
                    val endParts = endDate.split("/")
                    if (startParts.size == 3 && endParts.size == 3) {
                      try {
                        calendar.set(
                            startParts[2].toInt(), startParts[1].toInt() - 1, startParts[0].toInt())
                        val startTimestamp = Timestamp(calendar.time)
                        calendar.set(
                            endParts[2].toInt(), endParts[1].toInt() - 1, endParts[0].toInt())
                        val endTimestamp = Timestamp(calendar.time)

                        val trip =
                            Trip(
                                id = tripsViewModel.getNewTripId(),
                                creator = creator,
                                participants = participants.split(",").map { it.trim() }.toList(),
                                description = description,
                                name = name,
                                locations =
                                    locations
                                        .split(";")
                                        .map { locationString ->
                                          val parts = locationString.split(",").map { it.trim() }
                                          if (parts.size >= 2) {
                                            Location(
                                                country = parts[0],
                                                city = parts[1],
                                                county = parts.getOrNull(2),
                                                zip = parts.getOrNull(3))
                                          } else {
                                            Location(
                                                country = "Unknown",
                                                city = "Unknown",
                                                county = null,
                                                zip = null)
                                          }
                                        }
                                        .toList(),
                                startDate = startTimestamp,
                                endDate = endTimestamp,
                                activities = listOf(),
                                type = tripType)
                        tripsViewModel.createTrip(trip)
                        navigationActions.goBack()
                      } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Invalid date format", Toast.LENGTH_SHORT).show()
                      }
                    } else {
                      Toast.makeText(
                              context, "Please enter valid start/end dates", Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  enabled = name.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank(),
                  modifier = Modifier.fillMaxWidth()) {
                    Text("Save Trip")
                  }
            }
      })
}

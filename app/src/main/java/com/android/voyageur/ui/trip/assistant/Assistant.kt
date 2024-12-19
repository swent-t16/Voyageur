package com.android.voyageur.ui.trip.assistant

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.assistant.convertActivityFromAssistantToActivity
import com.android.voyageur.model.assistant.extractActivitiesFromAssistantFromJson
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.trip.activities.ActivityItem
import com.android.voyageur.ui.trip.activities.ButtonType
import com.android.voyageur.ui.trip.schedule.TopBarWithImageAndText
import com.google.firebase.Timestamp

/**
 * The assistant screen that allows the user to ask the assistant for activities for a trip. The
 * user can provide a prompt and the assistant will generate activities based on that prompt. The
 * user can also provide settings for the assistant. The user can also add the activities directly
 * to the trip.
 *
 * @param tripsViewModel the view model for the trips
 * @param navigationActions the navigation actions
 * @param userViewModel the view model for the user
 */
@SuppressLint("StateFlowValueCalledInComposition", "UnrememberedMutableState")
@Composable
fun AssistantScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val trip = tripsViewModel.selectedTrip.value
  if (trip == null) {
    navigationActions.navigateTo(Screen.OVERVIEW)
    return
  }

  // State
  var result by rememberSaveable { mutableStateOf("placeholderResult") }
  val uiState by tripsViewModel.uiState.collectAsState()
  var activities by remember { mutableStateOf(emptyList<Activity>()) }
  var addButtonWasClicked by remember { mutableStateOf(false) }

  // User related data
  var prompt by rememberSaveable { mutableStateOf("") }
  val userInterests = userViewModel.user.collectAsState().value?.interests ?: emptyList()
  val participantsInterests =
      trip.participants
          .mapNotNull { participant ->
            userViewModel.contacts.value.find { it.id == participant } // Find user
          }
          .flatMap { user -> user.interests } // Extract all interests
          .distinct() // Keep only distinct interests

  val keyboardController = LocalSoftwareKeyboardController.current

  // Settings
  var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
  var provideFinalActivities by rememberSaveable { mutableStateOf(false) }
  var useUserInterests by rememberSaveable { mutableStateOf(false) }
  var useParticipantsInterests by rememberSaveable { mutableStateOf(false) }

  // Context used to access resources when generating the prompt
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("assistantScreen"),
      content = { pd ->
        Column {
          TopBarWithImageAndText(
              trip, navigationActions, stringResource(R.string.ask_assistant), trip.name)

          Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
            // Text field to enter the prompt
            OutlinedTextField(
                value = prompt,
                label = { Text(stringResource(R.string.prompt)) },
                onValueChange = { newValue ->
                  // Prevent newlines in the text
                  if (!newValue.contains('\n')) {
                    prompt = newValue
                  }
                },
                maxLines = 3,
                modifier =
                    Modifier.testTag("AIRequestTextField")
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                        .onKeyEvent { event ->
                          if (event.key == Key.Enter) {
                            keyboardController?.hide()
                            if (uiState !is UiState.Loading) {
                              addButtonWasClicked = false
                              tripsViewModel.sendActivitiesPrompt(
                                  context = context,
                                  trip = trip,
                                  userPrompt = prompt,
                                  interests =
                                      if (useUserInterests) userInterests
                                      else if (useParticipantsInterests) participantsInterests
                                      else emptyList(),
                                  provideFinalActivities = provideFinalActivities,
                              )
                            }
                            true // Consume the key event.
                          } else {
                            false // Pass other key events through.
                          }
                        },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            // Button to press to send the prompt
            Button(
                onClick = {
                  addButtonWasClicked = false
                  tripsViewModel.sendActivitiesPrompt(
                      context = context,
                      trip = trip,
                      userPrompt = prompt,
                      interests =
                          if (useUserInterests) userInterests
                          else if (useParticipantsInterests) participantsInterests else emptyList(),
                      provideFinalActivities = provideFinalActivities,
                  )
                },
                enabled = uiState !is UiState.Loading, // Disable the button during loading
                modifier = Modifier.testTag("AIRequestButton").align(Alignment.CenterVertically)) {
                  Text(text = stringResource(R.string.ask))
                }

            // Button to open the settings dialog
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.testTag("settingsButton").align(Alignment.CenterVertically)) {
                  Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
          }

          when (uiState) {
            is UiState.Loading -> {
              // Show a loading indicator
              Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).testTag("loadingIndicator"))
              }
            }
            is UiState.Initial -> {
              // When entering the screen, show a text to prompt the user to ask the assistant
              Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.assistant_initial_screen),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.testTag("initialStateText").align(Alignment.Center).padding(16.dp))
              }
            }
            is UiState.Error -> {
              // Show an error message
              result = (uiState as UiState.Error).errorMessage
              Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.assistant_error),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("errorMessage").align(Alignment.Center))
              }
              Log.e("AssistantScreen", "Error: $result")
            }
            is UiState.Success -> {
              // Show the activities generated by the assistant
              result = (uiState as UiState.Success).outputText
              // Extract the activities from the assistant's response
              val activitiesFromAssistant = extractActivitiesFromAssistantFromJson(result)
              // Only update the activities if the Add button of any activity was not clicked. This
              // is to prevent the already added activities to appear on the screen when the screen
              // recomposes after clicking the go back button.
              if (!addButtonWasClicked) {
                // Convert the activities to the Activity model
                activities =
                    activitiesFromAssistant.map {
                      val activity = convertActivityFromAssistantToActivity(it)
                      if (!provideFinalActivities) {
                        activity.copy(startTime = Timestamp(0, 0), endTime = Timestamp(0, 0))
                      } else {
                        activity
                      }
                    }
              }

              if (activities.isEmpty()) {
                // Show a text when there are no activities
                Box(modifier = Modifier.fillMaxSize()) {
                  Text(
                      text = stringResource(R.string.assistant_no_activities),
                      textAlign = TextAlign.Center,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier =
                          Modifier.testTag("emptyActivitiesPrompt")
                              .align(Alignment.Center)
                              .padding(bottom = 80.dp, start = 16.dp, end = 16.dp))
                }
              } else {
                // Show the activities
                LazyColumn(
                    modifier =
                        Modifier.padding(pd)
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .testTag("lazyColumn"),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                ) {
                  items(activities) { activity ->
                    ActivityItem(
                        activity = activity,
                        false,
                        onClickButton = {
                          addButtonWasClicked = true
                          // remove this activity from the list
                          activities = activities.filter { it != activity }
                          // add this activity to the trip
                          tripsViewModel.addActivityToTrip(activity)
                        },
                        buttonPurpose = ButtonType.ADD,
                        navigationActions,
                        tripsViewModel)
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
              }
            }
          }

          if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false },
                provideDraftActivities = provideFinalActivities,
                onProvideFinalActivitiesChanged = { provideFinalActivities = it },
                useInterests = useUserInterests,
                onUseInterestsChanged = { useUserInterests = it },
                useParticipantsInterests = useParticipantsInterests,
                onUseParticipantsInterestsChanged = { useParticipantsInterests = it },
            )
          }
        }
      })
}

/**
 * Dialog for the settings. The user can change the settings for the assistant.
 *
 * @param onDismiss the function to dismiss the dialog
 * @param provideDraftActivities whether to provide final activities with date and time or just
 *   draft activities
 * @param onProvideFinalActivitiesChanged the function to change the setting
 * @param useInterests whether to use the user's interests
 * @param onUseInterestsChanged the function to change the setting
 * @param useParticipantsInterests whether to use the participants' interests
 * @param onUseParticipantsInterestsChanged the function to change the setting
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    provideDraftActivities: Boolean,
    onProvideFinalActivitiesChanged: (Boolean) -> Unit,
    useInterests: Boolean,
    onUseInterestsChanged: (Boolean) -> Unit,
    useParticipantsInterests: Boolean,
    onUseParticipantsInterestsChanged: (Boolean) -> Unit,
) {
  AlertDialog(
      modifier = Modifier.testTag("settingsDialog"),
      onDismissRequest = onDismiss,
      title = { Text(text = stringResource(R.string.settings)) },
      text = {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Explain the settings
            Text(
                stringResource(R.string.settings_subtitle),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge)
          }
          Spacer(modifier = Modifier.height(16.dp))

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch to provide final activities
                Text(
                    stringResource(R.string.provide_final_activities),
                    modifier = Modifier.weight(1f))
                Switch(
                    checked = provideDraftActivities,
                    onCheckedChange = onProvideFinalActivitiesChanged,
                    modifier = Modifier.testTag("provideFinalActivitiesSwitch"))
              }
          Spacer(modifier = Modifier.height(4.dp))

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch to use only the current user's interests
                Text(stringResource(R.string.use_user_interests), modifier = Modifier.weight(1f))
                Switch(
                    checked = useInterests,
                    onCheckedChange = { isChecked ->
                      onUseInterestsChanged(isChecked)
                      if (isChecked) {
                        onUseParticipantsInterestsChanged(false) // Turn off the other switch
                      }
                    },
                    modifier = Modifier.testTag("useUserInterestsSwitch"))
              }
          Spacer(modifier = Modifier.height(4.dp))

          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch to use the participants' interests
                Text(
                    stringResource(R.string.use_participants_interests),
                    modifier = Modifier.weight(1f))
                Switch(
                    checked = useParticipantsInterests,
                    onCheckedChange = { isChecked ->
                      onUseParticipantsInterestsChanged(isChecked)
                      if (isChecked) {
                        onUseInterestsChanged(false) // Turn off the other switch
                      }
                    },
                    modifier = Modifier.testTag("useParticipantsInterestsSwitch"))
              }
        }
      },
      confirmButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("closeDialogButton")) {
          Text(stringResource(R.string.close))
        }
      })
}

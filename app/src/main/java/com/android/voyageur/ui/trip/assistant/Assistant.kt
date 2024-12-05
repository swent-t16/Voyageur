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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * user can also provide settings for the assistant. The user can also add the activities direrctly
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
  var result by rememberSaveable { mutableStateOf("placeholderResult") }
  val uiState by tripsViewModel.uiState.collectAsState()
  var prompt by rememberSaveable { mutableStateOf("") }
  var activities by remember { mutableStateOf(emptyList<Activity>()) }
  Log.d("AssistantScreen", userViewModel.user.toString())
  Log.d("AssistantScreen", userViewModel.user.collectAsState().toString())
  Log.d("AssistantScreen", userViewModel.user.collectAsState().value.toString())
  Log.d("AssistantScreen", userViewModel.user.collectAsState().value?.interests.toString())

  val interests = userViewModel.user.collectAsState().value?.interests ?: emptyList()
  val keyboardController = LocalSoftwareKeyboardController.current

  var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
  var provideFinalActivities by rememberSaveable { mutableStateOf(false) }
  var useInterests by rememberSaveable { mutableStateOf(false) }

  val trip = tripsViewModel.selectedTrip.value
  if (trip == null) {
    navigationActions.navigateTo(Screen.OVERVIEW)
    return
  }

  Scaffold(
      modifier = Modifier.testTag("assistantScreen"),
      content = { pd ->
        Column {
          TopBarWithImageAndText(
              trip, navigationActions, stringResource(R.string.ask_assistant), trip.name)
          Row(modifier = Modifier.padding(all = 16.dp)) {
            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.prompt)) },
                onValueChange = { prompt = it },
                modifier =
                    Modifier.testTag("AIRequestTextField")
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                          keyboardController?.hide()
                          if (uiState !is UiState.Loading) {
                            tripsViewModel.sendActivitiesPrompt(
                                trip = trip,
                                userPrompt = prompt,
                                interests = if (useInterests) interests else emptyList(),
                                provideFinalActivities = provideFinalActivities,
                            )
                          }
                        }),
                singleLine = true)
            Button(
                onClick = {
                  tripsViewModel.sendActivitiesPrompt(
                      trip = trip,
                      userPrompt = prompt,
                      interests = if (useInterests) interests else emptyList(),
                      provideFinalActivities = provideFinalActivities,
                  )
                },
                enabled = uiState !is UiState.Loading, // Disable the button during loading
                modifier = Modifier.testTag("AIRequestButton").align(Alignment.CenterVertically)) {
                  Text(text = stringResource(R.string.go))
                }
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.testTag("settingsButton")) {
                  Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
          }

          if (uiState is UiState.Loading) {
            Box(modifier = Modifier.fillMaxSize().testTag("loadingIndicator")) {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
          } else {
            val scrollState = rememberScrollState()
            if (uiState is UiState.Error) {
              result = (uiState as UiState.Error).errorMessage
              Text(
                  text = result,
                  textAlign = TextAlign.Start,
                  color = MaterialTheme.colorScheme.error,
                  modifier =
                      Modifier.testTag("errorMessage")
                          .align(Alignment.CenterHorizontally)
                          .padding(16.dp)
                          .fillMaxSize()
                          .verticalScroll(scrollState))
            } else if (uiState is UiState.Success) {
              result = (uiState as UiState.Success).outputText
              val activitiesFromAssistant = extractActivitiesFromAssistantFromJson(result)
              activities =
                  activitiesFromAssistant.map {
                    val activity = convertActivityFromAssistantToActivity(it)
                    if (!provideFinalActivities) {
                      activity.copy(startTime = Timestamp(0, 0), endTime = Timestamp(0, 0))
                    } else {
                      activity
                    }
                  }
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
              useInterests = useInterests,
              onUseInterestsChanged = { useInterests = it })
        }
      })
}

/**
 * Dialog for the settings
 *
 * @param onDismiss the function to dismiss the dialog
 * @param provideDraftActivities whether to provide final activities with date and time or just
 *   draft activities
 * @param onProvideFinalActivitiesChanged the function to change the setting
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    provideDraftActivities: Boolean,
    onProvideFinalActivitiesChanged: (Boolean) -> Unit,
    useInterests: Boolean,
    onUseInterestsChanged: (Boolean) -> Unit
) {
  AlertDialog(
      modifier = Modifier.testTag("settingsDialog"),
      onDismissRequest = onDismiss,
      title = { Text(text = stringResource(R.string.settings)) },
      text = {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.settings_subtitle),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge)
          }
          Spacer(modifier = Modifier.height(16.dp)) // Add a spacer for some vertical space

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.provide_final_activities), modifier = Modifier.weight(1f))
            Switch(
                checked = provideDraftActivities,
                onCheckedChange = onProvideFinalActivitiesChanged,
                modifier = Modifier.testTag("provideFinalActivitiesSwitch"))
          }
          Spacer(modifier = Modifier.height(16.dp)) // Add a spacer for some vertical space
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.use_interests), modifier = Modifier.weight(1f))
            Switch(
                checked = useInterests,
                onCheckedChange = onUseInterestsChanged,
                modifier = Modifier.testTag("useInterestsSwitch"))
          }
        }
      },
      confirmButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("closeDialogButton")) {
          Text(stringResource(R.string.close))
        }
      })
}

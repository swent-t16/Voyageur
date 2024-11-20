package com.android.voyageur.ui.trip.assistant

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.extractActivitiesFromJson
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.trip.UiState
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.activities.ActivityItem
import com.android.voyageur.ui.trip.activities.ButtonType
import com.android.voyageur.ui.trip.schedule.TopBarWithImageAndText

@Composable
fun AssistantScreen(tripsViewModel: TripsViewModel, navigationActions: NavigationActions) {
  var result by rememberSaveable { mutableStateOf("placeholderResult") }
  val uiState by tripsViewModel.uiState.collectAsState()
  var prompt by rememberSaveable { mutableStateOf("") }
  var activities by remember { mutableStateOf(emptyList<Activity>()) }

  val trip =
      tripsViewModel.selectedTrip.value ?: return Text(text = "No trip selected. Should not happen")

  Scaffold(
      modifier = Modifier.testTag("assistantScreen"),
      content = { pd ->
        Column {
          TopBarWithImageAndText(trip, navigationActions, "Ask the AI assistant!", trip.name)
          Row(modifier = Modifier.padding(all = 16.dp)) {
            TextField(
                value = prompt,
                label = { Text("prompt") },
                onValueChange = { prompt = it },
                modifier =
                    Modifier.testTag("AIRequestTextField")
                        .weight(0.8f)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically))
            Button(
                onClick = { tripsViewModel.sendActivitiesPrompt(trip, prompt) },
                modifier = Modifier.testTag("AIRequestButton").align(Alignment.CenterVertically)) {
                  Text(text = "go")
                }
          }

          if (uiState is UiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.testTag("loadingIndicator").align(Alignment.CenterHorizontally))
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
              activities = extractActivitiesFromJson(result)
              LazyColumn(
                  modifier =
                      Modifier.padding(pd)
                          .padding(top = 16.dp)
                          .fillMaxWidth()
                          .testTag("lazyColumn"),
                  verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
              ) {
                activities.forEach { activity ->
                  item {
                    ActivityItem(
                        activity = activity,
                        onClickButton = {
                          Log.d("NewActivitiesScreen", "Adding activity: $activity")
                          Log.d("NewActivitiesScreen", "Current activities: $activities")
                          activities = activities.filter { it != activity }
                          // add this activity to the trip
                          tripsViewModel.addActivityToTrip(activity)
                          Log.d("NewActivitiesScreen", trip.activities.toString())
                          //                        activities = activities.toMutableList().apply {
                          // remove(activity) }
                          Log.d("NewActivitiesScreen", "After removing: $activities")
                        },
                        buttonPurpose = ButtonType.ADD)
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
              }
            }
          }
        }
      })
}

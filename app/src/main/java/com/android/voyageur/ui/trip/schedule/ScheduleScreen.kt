package com.android.voyageur.ui.trip.schedule

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Displays the schedule for a trip, allowing the user to toggle between daily and weekly views.
 * Users can also interact with additional functionality like navigating to the assistant screen.
 *
 * The schedule adapts to the current state of the app:
 * - If `isDailyViewSelected` is `true`, the daily view is displayed using the `ByDayScreen`
 *   composable.
 * - If `isDailyViewSelected` is `false`, the weekly view is displayed using the `WeeklyViewScreen`
 *   composable.
 * - The "Ask Assistant" button is only visible when the app is not in read-only mode.
 *
 * @param tripsViewModel The ViewModel responsible for managing the trip's activities and state.
 * @param trip The trip whose schedule is being displayed, including activities and details.
 * @param navigationActions Handles navigation actions such as switching screens or views.
 * @param userViewModel The ViewModel responsible for user-specific data and state.
 * @param isReadOnly Boolean which determines if the user is in Read Only View and cannot access the
 *   AI assistant.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ScheduleScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    isReadOnly: Boolean = false
) {
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available
  val context = LocalContext.current

  Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp).testTag("scheduleScreen")) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          // "Ask Assistant" button on the left
          if (!isReadOnly) {
            TextButton(
                onClick = {
                  if (isConnected) {
                    tripsViewModel.setInitialUiState()
                    navigationActions.navigateTo(Screen.ASSISTANT)
                  } else {
                    Toast.makeText(
                            context, R.string.notification_no_internet_text, Toast.LENGTH_SHORT)
                        .show()
                  }
                },
                elevation = ButtonDefaults.elevatedButtonElevation(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(MaterialTheme.colorScheme.onPrimary)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.ask_assistant_button),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                  }
                }
          }
          Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 16.dp),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                      navigationActions.getNavigationState().isDailyViewSelected = true
                    }) {
                      Text(
                          text = stringResource(R.string.daily_text),
                          style = MaterialTheme.typography.bodyMedium,
                          color =
                              if (navigationActions.getNavigationState().isDailyViewSelected)
                                  MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                          fontWeight =
                              if (navigationActions.getNavigationState().isDailyViewSelected)
                                  FontWeight.Bold
                              else FontWeight.Normal)
                    }
                Text(
                    text = stringResource(R.string.slash_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                TextButton(
                    onClick = {
                      navigationActions.getNavigationState().isDailyViewSelected = false
                    }) {
                      Text(
                          text = stringResource(R.string.weekly_text),
                          style = MaterialTheme.typography.bodyMedium,
                          color =
                              if (!navigationActions.getNavigationState().isDailyViewSelected)
                                  MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                          fontWeight =
                              if (!navigationActions.getNavigationState().isDailyViewSelected)
                                  FontWeight.Bold
                              else FontWeight.Normal)
                    }
              }
        }

    // Conditionally show content based on isDailySelected
    if (navigationActions.getNavigationState().isDailyViewSelected) {
      ByDayScreen(
          tripsViewModel, trip, navigationActions, userViewModel = userViewModel, isReadOnly)
    } else {
      WeeklyViewScreen(
          tripsViewModel = tripsViewModel,
          trip = trip,
          navigationActions = navigationActions,
          userViewModel,
          isReadOnly)
    }
  }
}

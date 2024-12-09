package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen

@Composable
fun ScheduleScreen(
    tripsViewModel: TripsViewModel,
    trip: Trip,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
) {

  Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          // "Ask Assistant" button on the left
          if (!navigationActions.getNavigationState().isReadOnlyView) {
            TextButton(
                onClick = {
                  tripsViewModel.setInitialUiState()
                  navigationActions.navigateTo(Screen.ASSISTANT)
                }) {
                  Text(
                      text = "Ask Assistant",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold)
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
                          text = "Daily",
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
                    text = " / ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                TextButton(
                    onClick = {
                      navigationActions.getNavigationState().isDailyViewSelected = false
                    }) {
                      Text(
                          text = "Weekly",
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
      ByDayScreen(tripsViewModel, trip, navigationActions, userViewModel = userViewModel)
    } else {
      WeeklyViewScreen(
          tripsViewModel = tripsViewModel,
          trip = trip,
          navigationActions = navigationActions,
          userViewModel)
    }
  }
}

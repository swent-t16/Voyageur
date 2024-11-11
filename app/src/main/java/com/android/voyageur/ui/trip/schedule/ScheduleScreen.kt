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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.NavigationActions

@Composable
fun ScheduleScreen(trip: Trip, navigationActions: NavigationActions) {
  //  var isDailySelected by remember { mutableStateOf(navigationActions.isDailyViewSelected) }

  Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
    // Row for the "Daily" and "Weekly" buttons
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          TextButton(onClick = { navigationActions.isDailyViewSelected = true }) {
            Text(
                text = "Daily",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (navigationActions.isDailyViewSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight =
                    if (navigationActions.isDailyViewSelected) FontWeight.Bold
                    else FontWeight.Normal)
          }
          Text(
              text = " / ",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
          TextButton(onClick = { navigationActions.isDailyViewSelected = false }) {
            Text(
                text = "Weekly",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (!navigationActions.isDailyViewSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight =
                    if (!navigationActions.isDailyViewSelected) FontWeight.Bold
                    else FontWeight.Normal)
          }
        }

    // Conditionally show content based on isDailyViewSelected
    if (navigationActions.isDailyViewSelected) {
      ByDayScreen(trip, navigationActions)
    } else {
      WeeklyViewScreen(
          trip = trip,
          navigationActions = navigationActions,
          onDaySelected = { navigationActions.isDailyViewSelected = true })
    }
  }
}

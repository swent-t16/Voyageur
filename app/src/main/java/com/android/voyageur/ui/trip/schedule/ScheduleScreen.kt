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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.NavigationActions

@Composable
fun ScheduleScreen(trip: Trip, navigationActions: NavigationActions) {
  var isDailySelected by remember { mutableStateOf(true) }

  Column(modifier = Modifier.fillMaxSize()) {
    // Row for the "Daily" and "Weekly" buttons
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          TextButton(onClick = { isDailySelected = true }) {
            Text(
                text = "Daily",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDailySelected) Color.Black else Color.Gray,
                fontWeight = if (isDailySelected) FontWeight.Bold else FontWeight.Normal)
          }
          Text(text = " / ", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
          TextButton(onClick = { isDailySelected = false }) {
            Text(
                text = "Weekly",
                style = MaterialTheme.typography.bodyLarge,
                color = if (!isDailySelected) Color.Black else Color.Gray,
                fontWeight = if (!isDailySelected) FontWeight.Bold else FontWeight.Normal)
          }
        }

    // Conditionally show content based on isDailySelected
    if (isDailySelected) {
      ByDayScreen(trip, navigationActions)
    } else {
      WeeklyViewScreen(
          trip = trip,
          navigationActions = navigationActions,
          onDaySelected = { isDailySelected = true } // Add the callback here
          )
    }
  }
}

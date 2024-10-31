package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen

@Composable
fun DayWeekButton(navigationActions: NavigationActions) {

    var dayColor = Color.Gray
    var weekColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                navigationActions.navigateTo(Screen.BY_DAY)
                dayColor = Color.Black
                weekColor = Color.Gray
            }
        )
        {
            Text(
                text = "Daily",
                style = MaterialTheme.typography.bodyLarge,
                color = dayColor,
            )
        }
        Text(text = " / ", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        TextButton(
            onClick = {
                navigationActions.navigateTo(Screen.BY_WEEK)
                dayColor = Color.Gray
                weekColor = Color.Black
            }
        )
        {
            Text(
                text = "Weekly",
                style = MaterialTheme.typography.bodyLarge,
                color = weekColor,
            )
        }
    }
}

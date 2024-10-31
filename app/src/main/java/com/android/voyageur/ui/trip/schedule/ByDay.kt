package com.android.voyageur.ui.trip.schedule

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.location.Location
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TRIP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.android.voyageur.ui.overview.toDateString
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.MonthDay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ByDayScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
) {
  val trip =
      tripsViewModel.selectedTrip.collectAsState().value
          ?: return Text(text = "No ToDo selected. Should not happen", color = Color.Red)
  Scaffold(
      // TODO: Final implementation of ByDayScreen
      modifier = Modifier.testTag("byDayScreen"),
      topBar = {
          TopBarWithImage(selectedTrip = trip, navigationActions = navigationActions)
         /**   TopAppBar(
                modifier = Modifier.testTag("topBar"),
                title = { Text("Schedule ByDay:") },
                navigationIcon = {
                  IconButton(
                      modifier = Modifier.testTag("backToOverviewButton"),
                      onClick = { navigationActions.navigateTo(Screen.OVERVIEW) }) {
                        Icon(imageVector = Icons.Outlined.Home, contentDescription = "Home")
                      }
                })
         */
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TRIP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
          //TODO: HARDCODED ACTIVITY LIST to be removed when we can add an activity
          //TODO: sort activities by their hour
          val fakeActivities = listOf(Activity(
              title = "Breakfast",
              description ="",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 20.25,
              location = Location()
          ), Activity(
              title = "Dinner",
              description ="",
              activityType = ActivityType.RESTAURANT,
              startTime = Timestamp.now(),
              endDate = Timestamp.now(),
              estimatedPrice = 23.25,
              location = Location()
          ),
              Activity(
                  title = "Too long name to be displayed on the Activity Box",
                  description ="",
                  activityType = ActivityType.OTHER,
                  startTime = Timestamp.now(),
                  endDate = Timestamp.now(),
                  estimatedPrice = 00.00,
                  location = Location()
              ))
          val groupedActivities = groupActivitiesByDate(fakeActivities)
          Log.e(fakeActivities.toString(), "Exist")
          if (groupedActivities.isEmpty()) {
              Text(
                  modifier = Modifier
                      .padding(pd)
                      .testTag("emptyByDayPrompt"),
                  text =
                  "You have no activities yet. Schedule one."
              )
          }else
          {
              LazyColumn(
                  modifier = Modifier
                      .padding(pd) // Use Scaffold’s padding
                      .padding(top = 16.dp) // Additional padding at the top
                      .fillMaxWidth(), // Fill the available width
                  verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                  groupedActivities.forEach{
                          (day, activitiesForDay) ->
                      item {
                          DayActivityCard(day, activitiesForDay)
                          Spacer(modifier = Modifier.height(10.dp))
                      }
                  }
              }
          }
      })
}
//TODO: Test this works
fun groupActivitiesByDate(activities: List<Activity>): Map<LocalDate, List<Activity>>
{
    return activities.groupBy { activity ->
        activity.startTime.toDate().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
@Composable
fun DayActivityCard(day:LocalDate, activitiesForDay: List<Activity>)
{
    Card(onClick = {},
        modifier =
            Modifier.width(353.dp)
                .height(228.dp)
                .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                .testTag("cardItem"),
        shape = RoundedCornerShape(16.dp),
        content = {
            Text(text = formatDate(day),
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF000000),
                    letterSpacing = 0.14.sp,
                ),
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 10.dp)
            ) {
                activitiesForDay.forEach{
                        activity ->
                    item {
                        ActivityBox(activity)
                    }
                }
            }
        }
    )

}

@Composable
fun ActivityBox(activity: Activity)
{   val backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White
    Box(
        modifier = Modifier
            .width(119.dp)
            .height(19.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 20.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = activity.title,
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(500),
                letterSpacing = 0.1.sp,
            ),
            maxLines = 1, // Limit to a single line
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 10.dp))
    }
}

// Function to format LocalDate to a "Day, d Month" format
fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
    return date.format(formatter)
}


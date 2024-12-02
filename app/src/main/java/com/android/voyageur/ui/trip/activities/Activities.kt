package com.android.voyageur.ui.trip.activities

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
/**
 * The main screen for managing and displaying activities in a trip. It provides two categorized
 * lists: drafts and final activities. The screen includes filtering by type, navigation, and and
 * options to edit and delete activities.
 *
 * @param navigationActions Provides actions for navigating between screens.
 * @param userViewModel The [UserViewModel] instance for managing user-related data.
 * @param tripsViewModel The [TripsViewModel] instance for accessing trip and activity data.
 */
fun ActivitiesScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    tripsViewModel: TripsViewModel
) {
    // States for filtering
    var selectedFilters by remember { mutableStateOf(setOf<ActivityType>()) }
    var showFilterMenu by remember { mutableStateOf(false) }

    var drafts by remember {
        mutableStateOf(
            tripsViewModel.getActivitiesForSelectedTrip().filter { activity ->
                activity.startTime == Timestamp(0, 0) || activity.endTime == Timestamp(0, 0)
            })
    }
    var final by remember {
        mutableStateOf(
            tripsViewModel
                .getActivitiesForSelectedTrip()
                .filter { activity ->
                    activity.startTime != Timestamp(0, 0) && activity.endTime != Timestamp(0, 0)
                }
                .sortedWith(
                    compareBy(
                        { it.startTime }, // First, sort by startTime
                        { it.endTime } // If startTime is equal, sort by endTime
                    )))
    }

    var showDialog by remember { mutableStateOf(false) }
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }
    var totalEstimatedPrice by remember { mutableStateOf(0.0) }

    LaunchedEffect(final, selectedFilters) {
        totalEstimatedPrice = final
            .filter { activity -> selectedFilters.isEmpty() || activity.activityType in selectedFilters }
            .sumOf { it.estimatedPrice }
    }

    Scaffold(
        modifier = Modifier.testTag("activitiesScreen"),
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute(),
                userViewModel = userViewModel)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Activities bar")
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("filterButton"),
                        onClick = { showFilterMenu = true },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = stringResource(R.string.filter_activities),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        })
                })
        },
        floatingActionButton = { AddActivityButton(navigationActions) },
        content = { pd ->
            LazyColumn(
                modifier =
                Modifier.padding(pd).padding(top = 16.dp).fillMaxWidth().testTag("lazyColumn"),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.drafts),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp))
                }
                drafts.forEach { activity ->
                    item {
                        if (selectedFilters.isEmpty() || activity.activityType in selectedFilters) {
                            ActivityItem(
                                activity,
                                true,
                                onClickButton = {
                                    activityToDelete = activity
                                    showDialog = true
                                },
                                ButtonType.DELETE,
                                navigationActions,
                                tripsViewModel)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.final_activities),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp))
                }
                final.forEach { activity ->
                    item {
                        if (selectedFilters.isEmpty() || activity.activityType in selectedFilters) {
                            ActivityItem(
                                activity,
                                true,
                                onClickButton = {
                                    activityToDelete = activity
                                    showDialog = true
                                },
                                ButtonType.DELETE,
                                navigationActions,
                                tripsViewModel)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                item {
                    androidx.compose.foundation.layout.Box(
                        modifier =
                        Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(16.dp) // Inner padding for content within the box
                            .testTag("totalEstimatedPriceBox"),
                        contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.total_price, totalEstimatedPrice),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center))
                    }
                }
            }

            if (showDialog) {
                DeleteActivityAlertDialog(
                    onDismissRequest = { showDialog = false },
                    activityToDelete = activityToDelete,
                    tripsViewModel = tripsViewModel,
                    confirmButtonOnClick = {
                        showDialog = false
                        final = final.filter { it != activityToDelete }
                        drafts = drafts.filter { it != activityToDelete }
                    })
            }
            if (showFilterMenu) {
                FilterDialog(
                    selectedFilters = selectedFilters,
                    onFilterChanged = { filter, isSelected ->
                        selectedFilters =
                            if (isSelected) {
                                selectedFilters + filter
                            } else {
                                selectedFilters - filter
                            }
                    },
                    onDismiss = { showFilterMenu = false })
            }
        })
}

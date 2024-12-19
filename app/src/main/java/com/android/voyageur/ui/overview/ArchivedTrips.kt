package com.android.voyageur.ui.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.voyageur.R
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions

/**
 * Composable function that renders the archive trips screen.
 *
 * This screen displays a list of archived trips and allows users to unarchive them.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedTripsScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
) {
  val trips by tripsViewModel.trips.collectAsState()
  // Explicitly sort archived trips by start date
  val archivedTrips = trips.filter { it.archived }.sortedBy { it.startDate }
  val isLoading by userViewModel.isLoading.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.archived_trips)) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backFromArchiveButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back))
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel,
            tripsViewModel = tripsViewModel)
      }) { padding ->
        if (isLoading) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
          }
        } else {
          if (archivedTrips.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center) {
                  Text(
                      modifier = Modifier.testTag("emptyArchivePrompt"),
                      text = stringResource(R.string.no_archived_trips),
                      style = MaterialTheme.typography.bodyLarge)
                }
          } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().testTag("archivedTripsColumn"),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  items(archivedTrips.size) { index ->
                    userViewModel._user.value?.let { user ->
                      TripItem(
                          tripsViewModel = tripsViewModel,
                          trip = archivedTrips[index],
                          navigationActions = navigationActions,
                          userViewModel = userViewModel,
                          user = user)
                    }
                  }
                }
          }
        }
      }
}

/** Composable button for accessing archived trips. */
@Composable
fun ArchiveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(
      onClick = onClick,
      modifier = modifier.testTag("archiveButton"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Outlined.Archive,
                  contentDescription = stringResource(R.string.archive))
              Text(stringResource(R.string.archived_trips))
            }
      }
}

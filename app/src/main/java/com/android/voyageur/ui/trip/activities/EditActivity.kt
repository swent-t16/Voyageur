package com.android.voyageur.ui.trip.activities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.trip.AddActivityScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivityScreen(
    navigationActions: NavigationActions,
    tripsViewModel: TripsViewModel,
) {
  val activity by tripsViewModel.selectedActivity.collectAsState()
  val selectedActivity = activity!!

  Scaffold(
      modifier = Modifier.testTag("editActivityScreen"),
      content = { pd ->
        Box(modifier = Modifier.padding(pd)) {
          AddActivityScreen(tripsViewModel, navigationActions, existingActivity = selectedActivity)
        }
      })
}

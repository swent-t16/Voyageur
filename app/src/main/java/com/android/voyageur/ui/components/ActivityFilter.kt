package com.android.voyageur.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.voyageur.R
import com.android.voyageur.model.activity.ActivityType
import com.android.voyageur.ui.trip.activities.FilterDialog

/**
 * A reusable filter component that manages activity type filtering. It provides a filter button and
 * dialog for selecting activity types.
 *
 * @param selectedFilters The currently selected activity type filters
 * @param onFiltersChanged Callback invoked when filters are modified
 * @param modifier Optional modifier for customizing the component's layout
 */
@Composable
fun ActivityFilter(
    selectedFilters: Set<ActivityType>,
    onFiltersChanged: (Set<ActivityType>) -> Unit,
    modifier: Modifier = Modifier
) {
  var showFilterMenu by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
    IconButton(modifier = Modifier.testTag("filterButton"), onClick = { showFilterMenu = true }) {
      Icon(
          imageVector = Icons.Outlined.FilterAlt,
          contentDescription = stringResource(R.string.filter_activities),
          tint = MaterialTheme.colorScheme.primary)
    }
  }

  if (showFilterMenu) {
    FilterDialog(
        selectedFilters = selectedFilters,
        onFilterChanged = { filter, isSelected ->
          val newFilters =
              if (isSelected) {
                selectedFilters + filter
              } else {
                selectedFilters - filter
              }
          onFiltersChanged(newFilters)
        },
        onDismiss = { showFilterMenu = false })
  }
}

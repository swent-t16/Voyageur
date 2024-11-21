package com.android.voyageur.ui.trip.activities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen

/** Composable that displays a button that navigates to the Add Activity screen. */
@Composable
fun AddActivityButton(navigationActions: NavigationActions) {
    FloatingActionButton(
        onClick = { navigationActions.navigateTo(Screen.ADD_ACTIVITY) },
        modifier = Modifier.testTag("createActivityButton")) {
        Icon(Icons.Outlined.Add, "Floating action button", modifier = Modifier.testTag("addIcon"))
    }
}

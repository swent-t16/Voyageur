package com.android.voyageur.ui.trip.photos

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.gallery.PermissionButtonForGallery
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@SuppressLint("StateFlowValueCalledInComposition", "MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun PhotosScreen(
    tripsViewModel: TripsViewModel,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel
) {
  val trip by tripsViewModel.selectedTrip.collectAsState()
  val photos = trip?.photos ?: emptyList()
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  Scaffold(
      floatingActionButton = {
        PermissionButtonForGallery(
            onUriSelected = { uri ->
              uri?.let {
                if (isConnected) {
                  val newPhotoUri = uri.toString()
                  tripsViewModel.addPhotoToTrip(newPhotoUri)
                  Toast.makeText(context, "Photo successfully added", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                }
              }
            },
            messageToShow = "Add Photo",
            dialogMessage = "We need permission to access your gallery.")
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel)
      },
      topBar = {
        TopAppBar(
            title = { Text("Photos") },
        )
      },
      content = { pd ->
        if (photos.isEmpty()) {
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyPhotosPrompt"),
                text = "You have no photos yet.",
            )
          }
        } else {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
                photos.forEach { photo ->
                  item {
                    PhotoItem(photoUri = photo, tripsViewModel = tripsViewModel)
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
              }
        }
      })
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun PhotoItem(photoUri: String, tripsViewModel: TripsViewModel) {
  var isExpanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available
  Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(8.dp).fillMaxWidth()) {
    Image(
        painter = rememberAsyncImagePainter(photoUri),
        contentDescription = "Trip Photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(200.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
      IconButton(
          enabled = isConnected,
          onClick = { isExpanded = !isExpanded },
          modifier = Modifier.align(Alignment.TopEnd).testTag("expandIcon_${photoUri}")) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = if (isExpanded) "Collapse" else "Expand")
          }
      DropdownMenu(
          expanded = isExpanded,
          onDismissRequest = { isExpanded = false },
          modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            DropdownMenuItem(
                onClick = {
                  isExpanded = false
                  showDialog = true
                },
                text = { Text("Delete") },
                modifier = Modifier.testTag("deleteMenuItem_${photoUri}"))
          }
    }
  }
  // Confirmation Dialog
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(text = "Remove Photo") },
        text = { Text("Are you sure you want to remove this photo?") },
        confirmButton = {
          TextButton(
              onClick = {
                if (isConnected) {
                  tripsViewModel.removePhotoFromTrip(photoUri)
                  Toast.makeText(context, "Photo successfully deleted", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                }
                showDialog = false
              }) {
                Text("Remove")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
  }
}

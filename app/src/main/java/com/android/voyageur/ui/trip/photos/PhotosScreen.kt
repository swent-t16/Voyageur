package com.android.voyageur.ui.trip.photos

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@SuppressLint("StateFlowValueCalledInComposition", "MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun PhotosScreen(tripsViewModel: TripsViewModel, navigationActions: NavigationActions) {
  val trip = tripsViewModel.selectedTrip.value!!
  val photos by remember { mutableStateOf(trip.photos.toMutableList()) }
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              if (isConnected) {
                // Implement photo upload functionality
                // Here, you can use an image picker to get the photo URL
                val newPhotoUrl =
                    "https://example.com/newphoto.jpg" // Replace with actual upload logic
                val updatedPhotos = trip.photos + newPhotoUrl
                val updatedTrip = trip.copy(photos = updatedPhotos)
                tripsViewModel.updateTrip(
                    updatedTrip,
                    onSuccess = {
                      /*
                          This is a trick to force a recompose, because the reference wouldn't
                          change and update the UI.
                      */
                      tripsViewModel.selectTrip(Trip())
                      tripsViewModel.selectTrip(updatedTrip)
                      navigationActions.goBack()
                    },
                    onFailure = { error ->
                      Toast.makeText(
                              context, "Failed to add photo: ${error.message}", Toast.LENGTH_SHORT)
                          .show()
                      Log.e("PhotosScreen", "Error adding photo: ${error.message}", error)
                    })
                Toast.makeText(context, "Photo added!", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
              }
            },
            content = { Icon(Icons.Outlined.Add, "Add Photo") })
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
              horizontalAlignment = Alignment.CenterHorizontally, // Center items horizontally
              modifier = Modifier.fillMaxSize().testTag("lazyColumn")) {
                photos.forEach { photo ->
                  item {
                    PhotoItem(photoUri = photo)
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }
              }
        }
      })
}

@Composable
fun PhotoItem(photoUri: String) {
  Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(8.dp).fillMaxWidth()) {
    Image(
        painter = rememberAsyncImagePainter(photoUri),
        contentDescription = "Trip Photo",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(200.dp))
  }
}

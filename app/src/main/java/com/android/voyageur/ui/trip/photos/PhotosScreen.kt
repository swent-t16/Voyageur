package com.android.voyageur.ui.trip.photos

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
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
  var selectedPhoto by remember { mutableStateOf<String?>(null) }
  val context = LocalContext.current
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  Scaffold(
      modifier = Modifier.testTag("photosScreen"),
      floatingActionButton = {
        PermissionButtonForGallery(
            onUriSelected = { uri ->
              uri?.let {
                if (isConnected) {
                  val imageUriParsed = Uri.parse(uri.toString())
                  tripsViewModel.uploadImageToFirebase(
                      uri = imageUriParsed,
                      onSuccess = { downloadUrl ->
                        tripsViewModel.addPhotoToTrip(downloadUrl)
                        Toast.makeText(context, "Photo successfully added", Toast.LENGTH_SHORT)
                            .show()
                      },
                      onFailure = { exception ->
                        Toast.makeText(
                                context,
                                "Failed to upload image: ${exception.message}",
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                } else {
                  Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                }
              }
            },
            messageToShow = "Add Photo",
            dialogMessage = "We need permission to access your gallery.",
            modifier = Modifier.testTag("addPhotoButton"))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
            userViewModel = userViewModel)
      },
      topBar = {
        TopAppBar(title = { Text("Photos") }, modifier = Modifier.testTag("photosTopBar"))
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
          LazyVerticalGrid(
              columns = GridCells.Fixed(4),
              contentPadding = PaddingValues(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier =
                  Modifier.padding(pd)
                      .padding(top = 16.dp, bottom = 60.dp)
                      .fillMaxSize()
                      .testTag("lazyVerticalGrid")) {
                items(photos.size) { index ->
                  val photoUri = photos[index]
                  PhotoThumbnail(photoUri) { selectedPhoto = photoUri }
                }
              }
          selectedPhoto?.let { photoUri ->
            val initialIndex = photos.indexOf(photoUri)
            PhotoDialog(
                photoUri = photoUri,
                photoList = photos,
                onDismiss = { selectedPhoto = null },
                initialIndex = initialIndex,
                tripsViewModel = tripsViewModel)
          }
        }
      })
}

@Composable
fun PhotoThumbnail(photoUri: String, onClick: () -> Unit) {
  Box(
      Modifier.size(80.dp)
          .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
          .clickable { onClick() }
          .testTag("photoThumbnail_${photoUri}"),
      contentAlignment = Alignment.Center) {
        Image(
            painter = rememberAsyncImagePainter(photoUri),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize())
      }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun PhotoDialog(
    photoUri: String,
    photoList: List<String>,
    onDismiss: () -> Unit,
    initialIndex: Int,
    tripsViewModel: TripsViewModel
) {
  var currentIndex by remember { mutableStateOf(initialIndex) }
  val currentPhoto = photoList.getOrNull(currentIndex)
  val context = LocalContext.current
  var showDialog by remember { mutableStateOf(false) }
  val status by connectivityState()
  val isConnected = status === ConnectionState.Available

  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.8f))
              .clickable { onDismiss() }
              .testTag("photoDialog_${photoUri}"),
      contentAlignment = Alignment.Center) {
        currentPhoto?.let {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.wrapContentSize()) {
                Box(modifier = Modifier.fillMaxWidth().wrapContentSize(align = Alignment.Center)) {
                  // Image
                  Image(
                      painter = rememberAsyncImagePainter(it),
                      contentDescription = "Full-size photo",
                      contentScale = ContentScale.Fit,
                      modifier = Modifier.fillMaxWidth().padding(16.dp).clickable {})

                  // Row to position the left and right buttons at the middle of the height
                  Row(
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                      modifier =
                          Modifier.fillMaxWidth()
                              .align(Alignment.Center) // Center the Row vertically
                      ) {
                        // Left Button ("<")
                        IconButton(
                            modifier = Modifier.testTag("goLeftButton"),
                            onClick = {
                              currentIndex = (currentIndex - 1 + photoList.size) % photoList.size
                            },
                            enabled = photoList.size > 1) {
                              Text("<", color = Color.White)
                            }

                        // Right Button (">")
                        IconButton(
                            modifier = Modifier.testTag("goRightButton"),
                            onClick = { currentIndex = (currentIndex + 1) % photoList.size },
                            enabled = photoList.size > 1) {
                              Text(">", color = Color.White)
                            }
                      }
                  // Delete button (bottom-right corner)
                  IconButton(
                      onClick = { showDialog = true },
                      modifier =
                          Modifier.align(Alignment.BottomEnd)
                              .padding(16.dp)
                              .testTag("deleteButton_${photoUri}")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Photo",
                            tint = Color.White)
                      }

                  // Download button (top-right corner)
                  IconButton(
                      onClick = {
                        // TODO: implement photo downloading functionality
                        Toast.makeText(context, "Photo downloaded", Toast.LENGTH_SHORT).show()
                      },
                      modifier =
                          Modifier.align(Alignment.TopEnd)
                              .padding(16.dp)
                              .testTag("downloadButton_${photoUri}")) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Photo",
                            tint = Color.White)
                      }
                }
              }
        }
      }
  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag("confirmDialog"),
        onDismissRequest = { showDialog = false },
        title = { Text(text = "Remove Photo") },
        text = { Text("Are you sure you want to remove this photo?") },
        confirmButton = {
          TextButton(
              onClick = {
                if (isConnected) {
                  tripsViewModel.removePhotoFromTrip(photoUri)
                  onDismiss()
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

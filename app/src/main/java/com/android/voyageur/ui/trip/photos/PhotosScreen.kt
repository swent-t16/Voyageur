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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.gallery.PermissionButtonForGallery
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Composable function to display the Photos screen of the app. This screen displays the photos
 * associated with a selected trip and allows the user to add, view, and delete photos.
 *
 * @param tripsViewModel [TripsViewModel] The ViewModel responsible for managing trips and their
 *   associated data.
 * @param navigationActions [NavigationActions] Used for handling navigation between screens.
 * @param userViewModel [UserViewModel] The ViewModel for managing user-related data.
 */
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
        if (selectedPhoto == null) {
          // Button to allow the user to add photos from the gallery
          PermissionButtonForGallery(
              enabled = isConnected,
              onUriSelected = { uri ->
                uri?.let {
                  // Upload the image to Firebase
                  val imageUriParsed = Uri.parse(uri.toString())
                  tripsViewModel.uploadImageToFirebase(
                      uri = imageUriParsed,
                      onSuccess = { downloadUrl ->
                        tripsViewModel.addPhotoToTrip(downloadUrl)
                        Toast.makeText(
                                context,
                                context.getString(R.string.photo_added),
                                Toast.LENGTH_SHORT)
                            .show()
                      },
                      onFailure = { exception ->
                        Toast.makeText(
                                context,
                                "Failed to upload image: ${exception.message}",
                                Toast.LENGTH_SHORT)
                            .show()
                      })
                }
              },
              messageToShow = stringResource(R.string.add_photo),
              dialogMessage = stringResource(R.string.gallery_permission),
              modifier = Modifier.testTag("addPhotoButton"),
              shouldCrop = false)
        }
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
            title = { Text(stringResource(R.string.photos)) },
            modifier = Modifier.testTag("photosTopBar"))
      },
      content = { pd ->
        if (photos.isEmpty()) {
          Box(modifier = Modifier.padding(pd).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.testTag("emptyPhotosPrompt"),
                text = stringResource(R.string.no_photos),
            )
          }
        } else {
          // Display photos in a grid layout
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

/**
 * Composable function to display a photo thumbnail.
 *
 * @param photoUri The URI of the photo to display.
 * @param onClick The callback when the thumbnail is clicked.
 */
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

/**
 * Composable function to display a dialog for viewing a full-size photo.
 *
 * @param photoUri The URI of the photo being displayed.
 * @param photoList The list of all photos associated with the trip.
 * @param onDismiss The callback to dismiss the dialog.
 * @param initialIndex The initial index of the photo in the list.
 * @param tripsViewModel The [TripsViewModel] to manage photo actions like deletion.
 */
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
                Box(modifier = Modifier.fillMaxSize().wrapContentSize(align = Alignment.Center)) {
                  // Image
                  Image(
                      painter = rememberAsyncImagePainter(it),
                      contentDescription = stringResource(R.string.full_size_photo),
                      contentScale = ContentScale.Fit,
                      modifier =
                          Modifier.fillMaxSize()
                              .padding(start = 40.dp, end = 40.dp, bottom = 60.dp, top = 64.dp)
                              .clickable {})
                  // Row to position the left and right buttons at the middle of the height
                  Row(
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                      modifier =
                          Modifier.fillMaxWidth()
                              .align(Alignment.Center) // Center the Row vertically
                      ) {
                        // Left Button
                        IconButton(
                            modifier = Modifier.testTag("goLeftButton"),
                            onClick = {
                              currentIndex = (currentIndex - 1 + photoList.size) % photoList.size
                            },
                            enabled = photoList.size > 1) {
                              Icon(
                                  imageVector = Icons.Default.ChevronLeft,
                                  contentDescription = stringResource(R.string.left_button),
                                  tint = Color.White)
                            }

                        // Right Button
                        IconButton(
                            modifier = Modifier.testTag("goRightButton"),
                            onClick = { currentIndex = (currentIndex + 1) % photoList.size },
                            enabled = photoList.size > 1) {
                              Icon(
                                  imageVector = Icons.Default.ChevronRight,
                                  contentDescription = stringResource(R.string.right_button),
                                  tint = Color.White)
                            }
                      }
                }
              }
        }
        // Delete button (bottom-right corner)
        IconButton(
            onClick = { showDialog = true },
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = 64.dp)
                    .testTag("deleteButton_${photoUri}")) {
              Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = stringResource(R.string.delete_photo),
                  tint = Color.White)
            }

        // Close button (top-right corner)
        IconButton(
            onClick = { onDismiss() },
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(top = 64.dp)
                    .testTag("closeButton_${photoUri}")) {
              Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = stringResource(R.string.close_photo),
                  tint = Color.White)
            }
      }
  // Alert dialog to confirm the deletion of the photo
  if (showDialog) {
    AlertDialog(
        modifier = Modifier.testTag("confirmDialog"),
        onDismissRequest = { showDialog = false },
        title = { Text(text = stringResource(R.string.remove_photo)) },
        text = { Text(stringResource(R.string.confirmation_delete_photo)) },
        confirmButton = {
          TextButton(
              enabled = isConnected,
              onClick = {
                tripsViewModel.removePhotoFromTrip(photoUri)
                onDismiss()
                Toast.makeText(
                        context, context.getString(R.string.photo_deleted), Toast.LENGTH_SHORT)
                    .show()
                showDialog = false
              }) {
                Text(stringResource(R.string.remove))
              }
        },
        dismissButton = {
          TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
        })
  }
}

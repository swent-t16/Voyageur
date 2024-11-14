package com.android.voyageur.ui.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.gallery.PermissionButtonForGallery
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.profile.interests.InterestChipEditable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  val user by userViewModel.user.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()
  // Check if user is null and navigate back to the Profile screen if true
  if (user == null && !isLoading) {
    navigationActions.navigateTo(Route.PROFILE)
    return
  }

  var name by remember { mutableStateOf(user?.name ?: "") }
  var email by remember { mutableStateOf(user?.email ?: "") }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
  var isSaving by remember { mutableStateOf(false) }

  // State variables for interests
  var interests by remember { mutableStateOf(user?.interests?.toMutableList() ?: mutableListOf()) }
  var newInterest by remember { mutableStateOf("") }

  if (isSaving) {
    LaunchedEffect(isSaving) {
      if (profilePictureUri != null) {
        userViewModel.updateUserProfilePicture(profilePictureUri!!) { downloadUrl ->
          val updatedUser =
              user!!.copy(name = name, profilePicture = downloadUrl, interests = interests)
          userViewModel.updateUser(updatedUser)
          isSaving = false
          navigationActions.navigateTo(Route.PROFILE)
        }
      } else {
        val updatedUser = user!!.copy(name = name, interests = interests)
        userViewModel.updateUser(updatedUser)
        isSaving = false
        navigationActions.navigateTo(Route.PROFILE)
      }
    }
  }

  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute(),
        )
      }) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
              } else {
                user?.let { userData ->
                  Column(
                      modifier = Modifier.fillMaxSize().padding(16.dp),
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        // Display the selected image or the existing profile picture
                        if (profilePictureUri != null) {
                          Image(
                              painter = rememberAsyncImagePainter(model = profilePictureUri),
                              contentDescription = "Profile Picture",
                              modifier =
                                  Modifier.size(128.dp).clip(CircleShape).testTag("profilePicture"))
                        } else if (userData.profilePicture.isNotEmpty()) {
                          Image(
                              painter = rememberAsyncImagePainter(model = userData.profilePicture),
                              contentDescription = "Profile Picture",
                              modifier =
                                  Modifier.size(128.dp).clip(CircleShape).testTag("profilePicture"))
                        } else {
                          Icon(
                              imageVector = Icons.Default.AccountCircle,
                              contentDescription = "Default Profile Picture",
                              modifier = Modifier.size(128.dp).testTag("defaultProfilePicture"))
                        }

                        PermissionButtonForGallery(
                            onUriSelected = { profilePictureUri = it },
                            "Edit Profile Picture",
                            "This app needs access to your photos to allow you to select a profile picture.",
                            1,
                            1,
                            Modifier.testTag("editImageButton"))

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.testTag("nameField"),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Email") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.testTag("emailField"))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display interests
                        Text(
                            text = "Interests:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp))

                        if (interests.isNotEmpty()) {
                          // Display interests using FlowRow for better layout
                          FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            interests.forEach { interest ->
                              InterestChipEditable(
                                  interest = interest,
                                  onRemove = {
                                    interests = interests.filter { it != interest }.toMutableList()
                                  })
                            }
                          }
                        } else {
                          // Display message when no interests are added
                          Text(
                              text = "No interests added yet",
                              style = MaterialTheme.typography.bodyMedium,
                              modifier = Modifier.testTag("noInterests"))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input field to add new interest
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          OutlinedTextField(
                              value = newInterest,
                              onValueChange = { newInterest = it },
                              label = { Text("Add Interest") },
                              maxLines = 1,
                              placeholder = { Text("Press Enter to add interest") },
                              modifier = Modifier.testTag("newInterestField"),
                              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                              keyboardActions =
                                  KeyboardActions(
                                      onDone = {
                                        if (newInterest.isNotBlank()) {
                                          if (!interests.contains(newInterest.trim())) {
                                            interests.add(newInterest.trim())
                                          }
                                          newInterest = ""
                                        }
                                      }))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { isSaving = true },
                            modifier = Modifier.testTag("saveButton")) {
                              Text("Save")
                            }
                      }
                }
                    ?: run {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No user data available",
                            modifier = Modifier.testTag("noUserData").padding(16.dp))
                      }
                    }
              }
            }
      }
}

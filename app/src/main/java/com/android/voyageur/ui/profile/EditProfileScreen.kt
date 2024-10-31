// EditProfileScreen.kt
package com.android.voyageur.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {
  val user by userViewModel.user.collectAsState()
  val isLoading by userViewModel.isLoading.collectAsState()

  var name by remember { mutableStateOf(user?.name ?: "") }
  var email by remember { mutableStateOf(user?.email ?: "") }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
  var profilePictureUrl by remember { mutableStateOf(user?.profilePicture ?: "") }
  var isSaving by remember { mutableStateOf(false) }

  val pickPhotoLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profilePictureUri = it }
      }

  if (isSaving) {
    LaunchedEffect(isSaving) {
      if (profilePictureUri != null) {
        userViewModel.updateUserProfilePicture(profilePictureUri!!) { downloadUrl ->
          val updatedUser = user!!.copy(name = name, profilePicture = downloadUrl)
          userViewModel.updateUser(updatedUser)
          isSaving = false
          navigationActions.navigateTo(Route.PROFILE)
        }
      } else {
        val updatedUser = user!!.copy(name = name)
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
      },
      content = { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              if (isLoading) {
                CircularProgressIndicator()
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
                              modifier = Modifier.size(128.dp).clip(CircleShape))
                        } else if (profilePictureUrl.isNotEmpty()) {
                          Image(
                              painter = rememberAsyncImagePainter(model = profilePictureUrl),
                              contentDescription = "Profile Picture",
                              modifier = Modifier.size(128.dp).clip(CircleShape))
                        } else {
                          Icon(
                              imageVector = Icons.Default.AccountCircle,
                              contentDescription = "Default Profile Picture",
                              modifier = Modifier.size(128.dp))
                        }

                        Button(onClick = { pickPhotoLauncher.launch("image/*") }) {
                          Text("Edit Profile Picture")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = name, onValueChange = { name = it }, label = { Text("Name") })

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Email") },
                            readOnly = true,
                            enabled = false)

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { isSaving = true }) { Text("Save") }
                      }
                } ?: run { Text("No user data available") }
              }
            }
      })
}

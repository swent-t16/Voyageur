package com.android.voyageur.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel, navigationActions: NavigationActions) {

    // Observe user and loading state from the UserViewModel
    val user by userViewModel.user.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var profilePicture by remember { mutableStateOf(user?.profilePicture ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // Set up the launcher for picking an image
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profilePicture = it.toString()
        }
    }

    if (isSaving) {
        LaunchedEffect(isSaving) {
            userViewModel.updateUser(
                User(id = user!!.id, name = name, email = email, profilePicture = profilePicture))
            isSaving = false
            navigationActions.navigateTo(Route.PROFILE)
        }
    }

    Scaffold(
        modifier = Modifier.testTag("editProfileScreen"),
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute(),
            )
        },
        content = { paddingValues ->
            Box(
                modifier =
                Modifier.fillMaxSize().padding(paddingValues).testTag("editProfileScreenContent"),
                contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                } else {
                    user?.let { userData ->
                        Column(
                            modifier =
                            Modifier.fillMaxSize().padding(16.dp).testTag("editProfileContent"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            if (profilePicture.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = profilePicture),
                                    contentDescription = "Profile Picture",
                                    modifier =
                                    Modifier.size(128.dp).clip(CircleShape).testTag("profilePicture"))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile Picture",
                                    modifier = Modifier.size(128.dp).testTag("defaultProfilePicture"))
                            }
                            Button(
                                onClick = { pickPhotoLauncher.launch("image/*") },
                                modifier = Modifier.testTag("editImageButton")) {
                                Text("Edit Profile Picture")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                modifier = Modifier.testTag("nameField"))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = {},
                                label = { Text("Email") },
                                readOnly = true, // Makes the field uneditable
                                enabled = false, // Disables the text field
                                modifier = Modifier.testTag("emailField")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { isSaving = true },
                                modifier = Modifier.testTag("saveButton")) {
                                Text("Save")
                            }
                        }
                    }
                        ?: run {
                            Text("No user data available", modifier = Modifier.testTag("noUserData"))
                        }
                }
            }
        })
}

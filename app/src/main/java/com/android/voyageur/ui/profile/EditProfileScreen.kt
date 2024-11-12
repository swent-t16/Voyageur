package com.android.voyageur.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
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

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrl by remember { mutableStateOf(user?.profilePicture ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // State variables for interests
    var interests by remember {
        mutableStateOf(
            user?.interests?.toMutableList() ?: mutableListOf()
        )
    }
    var newInterest by remember { mutableStateOf("") }

    if (isSaving) {
        LaunchedEffect(isSaving) {
            if (profilePictureUri != null) {
                userViewModel.updateUserProfilePicture(profilePictureUri!!) { downloadUrl ->
                    val updatedUser =
                        user!!.copy(
                            name = name,
                            profilePicture = downloadUrl,
                            interests = interests
                        )
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
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                } else {
                    user?.let { userData ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Display the selected image or the existing profile picture
                            if (profilePictureUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = profilePictureUri),
                                    contentDescription = "Profile Picture",
                                    modifier =
                                    Modifier
                                        .size(128.dp)
                                        .clip(CircleShape)
                                        .testTag("profilePicture")
                                )
                            } else if (profilePictureUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = profilePictureUrl),
                                    contentDescription = "Profile Picture",
                                    modifier =
                                    Modifier
                                        .size(128.dp)
                                        .clip(CircleShape)
                                        .testTag("profilePicture")
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile Picture",
                                    modifier = Modifier
                                        .size(128.dp)
                                        .testTag("defaultProfilePicture")
                                )
                            }
                            PermissionButtonForGallery(
                                onUriSelected = { profilePictureUri = it },
                                "Edit Profile Picture",
                                "This app needs access to your photos to allow you to select a profile picture.",
                                Modifier.testTag("editImageButton")

                            )

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
                                modifier = Modifier.testTag("emailField")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Display interests
                            Text(
                                text = "Interests:",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            if (interests.isNotEmpty()) {
                                // Display interests using FlowRow for better layout
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    interests.forEach { interest ->
                                        InterestChipEditable(
                                            interest = interest,
                                            onRemove = {
                                                interests = interests.filter { it != interest }
                                                    .toMutableList()
                                            })
                                    }
                                }
                            } else {
                                // Display message when no interests are added
                                Text(
                                    text = "No interests added yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.testTag("noInterests")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Input field to add new interest
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newInterest,
                                    onValueChange = { newInterest = it },
                                    label = { Text("Add Interest") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("newInterestField"),
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
                                        })
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        if (newInterest.isNotBlank()) {
                                            if (!interests.contains(newInterest.trim())) {
                                                interests.add(newInterest.trim())
                                            }
                                            newInterest = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("addInterestButton")
                                ) {
                                    Text("Add")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { isSaving = true },
                                modifier = Modifier.testTag("saveButton")
                            ) {
                                Text("Save")
                            }
                        }
                    }
                        ?: run {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No user data available",
                                    modifier = Modifier
                                        .testTag("noUserData")
                                        .padding(16.dp)
                                )
                            }
                        }
                }
            }
        })
}


package com.android.voyageur.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.android.voyageur.ui.profile.interests.InterestChip

/**
 * Composable function for displaying a detailed profile of a selected user.
 * Displays a loading indicator if data is loading and navigates back to search if no user data is available.
 *
 * @param userViewModel The UserViewModel used for managing user data and interactions.
 * @param navigationActions The NavigationActions used for handling navigation actions within the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserProfileScreen(
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
    // Observing the selected user and loading state from the ViewModel
    val user by userViewModel.selectedUser.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    // Navigate back to search if no user data is available and loading is complete
    if (user == null && !isLoading) {
        LaunchedEffect(Unit) { navigationActions.navigateTo(Route.SEARCH) }
        return
    }

    // Scaffold layout containing top bar for navigation and content for displaying user details
    Scaffold(
        modifier = Modifier.testTag("userProfileScreen"),
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        userViewModel.deselectUser() // Clears selected user in the ViewModel
                        navigationActions.goBack()   // Navigate back
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.testTag("userProfileBackButton")
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("userProfileScreenContent"),
                contentAlignment = Alignment.Center
            ) {
                // Display appropriate content based on loading and user state
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.testTag("userProfileLoadingIndicator"))
                    }
                    user != null -> {
                        SearchUserProfileContent(
                            userData = user!!,
                            userViewModel = userViewModel
                        )
                    }
                    else -> {
                        Text(
                            "No user data available",
                            modifier = Modifier.testTag("userProfileNoData")
                        )
                    }
                }
            }
        }
    )
}

/**
 * Composable function for displaying the details of a selected user.
 * Displays profile picture, name, email, interests, and an "Add Contact" button.
 *
 * @param userData The selected User object containing user details.
 * @param userViewModel The UserViewModel used for managing user data and interactions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchUserProfileContent(userData: User, userViewModel: UserViewModel) {
    // Observing if the selected user is already a contact
    val isContactAdded by remember {
        derivedStateOf { userViewModel.user.value?.contacts?.contains(userData.id) ?: false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("userProfileContent"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display profile picture or a default icon if the picture is unavailable
        if (userData.profilePicture.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = userData.profilePicture),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .testTag("userProfilePicture")
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .testTag("userProfileDefaultPicture")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display user's name and email or fallback text if unavailable
        Text(
            text = "Name: ${userData.name.takeIf { it.isNotEmpty() } ?: "No name available"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("userProfileName")
        )
        Text(
            text = "Email: ${userData.email.takeIf { it.isNotEmpty() } ?: "No email available"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("userProfileEmail")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display user's interests or fallback message if there are none
        Text(
            text = "Interests:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp).testTag("userProfileInterestsTitle")
        )

        if (userData.interests.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("userProfileInterestsFlow"),
                horizontalArrangement = Arrangement.Center
            ) {
                userData.interests.forEach { interest ->
                    InterestChip(interest = interest, modifier = Modifier.padding(4.dp))
                }
            }
        } else {
            Text(
                text = "No interests added yet",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("userProfileNoInterests")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Contact button, which is enabled only if the user is not already a contact
        Button(
            onClick = { userViewModel.addContact(userData.id) },
            enabled = !isContactAdded,
            modifier = Modifier.testTag("userProfileAddContactButton")
        ) {
            Text(text = if (isContactAdded) "Added" else "Add")
        }
    }
}

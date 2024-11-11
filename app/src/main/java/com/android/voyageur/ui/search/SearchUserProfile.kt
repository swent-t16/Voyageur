package com.android.voyageur.ui.search

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route

@Composable
fun SearchUserProfileScreen(
    userId: String,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
    // Fetch the user by userId
    val user by userViewModel.getUserById(userId).collectAsState(initial = null)
    val isLoading by userViewModel.isLoading.collectAsState()

    // Navigate back to SEARCH if user is null and not loading
    if (user == null && !isLoading) {
        LaunchedEffect(Unit) { navigationActions.navigateTo(Route.SEARCH) }
        return // Exit composable to prevent further execution
    }

    // Main Scaffold layout for ProfileScreen with Bottom Navigation
    Scaffold(
        modifier = Modifier.testTag("profileScreen"),
        content = { paddingValues ->
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("profileScreenContent"),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                    }
                    user != null -> {
                        SearchUserProfileContent(
                            userData = user!! // Force unwrap here
                        )
                    }
                    else -> {
                        Text(
                            "No user data available",
                            modifier = Modifier.testTag("noUserData")
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SearchUserProfileContent(userData: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profileContent"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the profile picture if available
        if (userData.profilePicture.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = userData.profilePicture),
                contentDescription = "Profile Picture",
                modifier = Modifier
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

        Spacer(modifier = Modifier.height(16.dp))

        // Display user name and email
        Text(
            text = "Name: ${userData.name.takeIf { it.isNotEmpty() } ?: "No name available"}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("userName")
        )
        Text(
            text = "Email: ${userData.email.takeIf { it.isNotEmpty() } ?: "No email available"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("userEmail")
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

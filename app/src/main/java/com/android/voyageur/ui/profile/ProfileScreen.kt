package com.android.voyageur.ui.profile

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
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navigationActions: NavigationActions) {
    // Get current Firebase user
    val currentUser = FirebaseAuth.getInstance().currentUser

    var isLoading by remember { mutableStateOf(true) }
    var isSigningOut by remember { mutableStateOf(false) }

    // Handle sign-out with navigation in a LaunchedEffect
    if (isSigningOut) {
        LaunchedEffect(isSigningOut) {
            delay(300) // Optional delay for smoother navigation
            navigationActions.navigateTo(Route.AUTH)
        }
    } else {
        if (currentUser != null) {
            isLoading = false
        } else {
            // If the user is not logged in, navigate to the sign-in screen
            navigationActions.navigateTo(Route.AUTH)
        }
    }

    // Main Scaffold layout for ProfileScreen with Bottom Navigation
    Scaffold(
        modifier = Modifier.testTag("profileScreen"),
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
                    .padding(paddingValues)
                    .testTag("profileScreenContent"),
                contentAlignment = Alignment.Center
            ) {
                if (isSigningOut) {
                    CircularProgressIndicator(modifier = Modifier.testTag("signingOutIndicator"))
                } else if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                } else {
                    currentUser?.let { userData ->
                        ProfileContent(
                            userData = userData,
                            onSignOut = {
                                FirebaseAuth.getInstance().signOut()
                                isSigningOut = true
                            }
                        )
                    } ?: run {
                        Text(text = "Failed to load user data.", modifier = Modifier.testTag("errorMessage"))
                    }
                }
            }
        }
    )
}

@Composable
fun ProfileContent(userData: FirebaseUser, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profileContent"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display profile picture if available
        if (userData.photoUrl.toString().isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(userData.photoUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .testTag("profilePicture")
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .testTag("defaultProfileIcon")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display user's name
        Text(
            text = userData.displayName ?: "No Name",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("profileName")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display user's email
        Text(
            text = userData.email ?: "No Email",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("profileEmail")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign out button
        Button(
            onClick = onSignOut,
            modifier = Modifier.testTag("signOutButton")
        ) {
            Text(text = "Sign Out")
        }
    }
}

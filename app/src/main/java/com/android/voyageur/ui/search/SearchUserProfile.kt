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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserProfileScreen(
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
    val user by userViewModel.selectedUser.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    if (user == null && !isLoading) {
        LaunchedEffect(Unit) { navigationActions.navigateTo(Route.SEARCH) }
        return
    }

    Scaffold(
        modifier = Modifier.testTag("userProfileScreen"),
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        userViewModel.deselectUser()
                        navigationActions.goBack()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchUserProfileContent(userData: User, userViewModel: UserViewModel) {
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

        Button(
            onClick = { userViewModel.addContact(userData.id) },
            enabled = !isContactAdded,
            modifier = Modifier.testTag("userProfileAddContactButton")
        ) {
            Text(text = if (isContactAdded) "Added" else "Add")
        }
    }
}

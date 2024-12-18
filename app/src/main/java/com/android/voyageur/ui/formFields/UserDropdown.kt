package com.android.voyageur.ui.formFields

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.trip.TripsViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.utils.ConnectionState
import com.android.voyageur.utils.connectivityState

/**
 * Composable function that displays the profile picture of a given user.
 *
 * This function renders a user's profile picture inside a circular avatar. If the user's profile
 * picture URI is not available or is empty, it falls back to displaying the first letter of the
 * user's name in uppercase, centered within the avatar circle.
 *
 * @param user An instance of the `User` class containing the user's profile data, including the
 *   `profilePicture` URI and `name`. *
 */
@Composable
fun UserIcon(user: User) {
  val profilePictureUri = user.profilePicture
  Box(
      modifier =
          Modifier.size(30.dp) // Set size for the avatar circle
              .testTag("participantAvatar")
              .background(Color.Gray, shape = RoundedCornerShape(50)), // Circular shape
      contentAlignment = Alignment.Center) {
        Image(
            painter = rememberAsyncImagePainter(profilePictureUri),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(50)).testTag("profilePic"))
      }
}

@Composable()
fun UserDropdown(
    users: List<Pair<User, Boolean>>,
    tripsViewModel: TripsViewModel,
    tripId: String,
    modifier: Modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart),
    onRemove: (Pair<User, Boolean>, Int) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

    val connectionStatus by connectivityState()
    val isConnected = connectionStatus == ConnectionState.Available

    // collect trip invites as stateflow
    val tripInvites by tripsViewModel.tripInvites.collectAsState()
    // collect the selected trip in case the user is in Edit Mode
    val selectedTrip by tripsViewModel.selectedTrip.collectAsState()


  Box(modifier = modifier) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(TextFieldDefaults.MinHeight)
                .border(
                    OutlinedTextFieldDefaults.UnfocusedBorderThickness,
                    MaterialTheme.colorScheme.outline)
                .padding(start = 14.dp)
                .clickable { expanded = true }
                .testTag("expander"),
        verticalAlignment = Alignment.CenterVertically) {
          val selectedUsers = users.filter { it.second }
          val numberOfUsers = selectedUsers.size
          if (selectedUsers.isEmpty()) {
            Text("Participants")
          }
          selectedUsers.forEach {
            Column(modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically)) {
              UserIcon(it.first)
            }
          }
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()) {
          users.forEachIndexed { index, userPair ->
              val user = userPair.first
              // Check if the user is already a participant
              var isAlreadyParticipant =
                  selectedTrip?.participants?.contains(user.id) == true

              // Find the invite if it exists
              val existingInvite = tripInvites.find { it.to == user.id && it.tripId == tripId }
              val isInvitePending = existingInvite != null


            DropdownMenuItem(
                text = { Text("${userPair.first.name} ") },
                onClick = { // Do nothing if pressing on the item (just on the buttons)
                     },
                trailingIcon = {
                    when {
                        isAlreadyParticipant -> {
                            Button(
                                onClick = {
                                    val updatedParticipants = selectedTrip?.participants?.filter { it != user.id }
                                    val updatedTrip = selectedTrip?.copy(participants = updatedParticipants ?: listOf())
                                    updatedTrip?.let {
                                        tripsViewModel.updateTrip(it)
                                    }
                                    onRemove(userPair, index)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove")
                            }
                        }

                        isInvitePending -> {
                            // Show "Cancel" button for pending invites
                            Button(
                                onClick = {
                                    existingInvite?.let { invite ->
                                        tripsViewModel.declineTripInvite(invite.id)
                                    }
                                },
                                modifier = Modifier.testTag("cancelButton_${user.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel")
                            }
                        }

                        else -> {
                            // Show "Send Invite" button
                            Button(
                                onClick = {
                                    tripsViewModel.sendTripInvite(tripId, user.id)
                                },
                                modifier = Modifier.testTag("inviteButton_${user.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Send Invite")
                            }
                        }
                    }
                }
            )
          }
    }
  }
}
package com.android.voyageur.ui.formFields

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
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
            contentDescription = stringResource(R.string.profile_picture_description),
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(50)).testTag("profilePic"))
      }
}

/**
 * Composable function to display a dropdown menu of users.
 *
 * This dropdown allows users to manage participants in a trip. Users can send invitations, cancel
 * pending invitations, or remove existing participants.
 *
 * ## Behavior
 * - If a user is already a participant, the "Remove" button will be displayed.
 * - If a trip invitation is pending, the "Cancel" button will be displayed.
 * - Otherwise, the "Invite" button will allow sending a trip invitation to the user.
 *
 * ## Values used
 * - connectionStatus and isConnected are used to disable the buttons in case the user is in Offline
 *   mode.
 * - selectedTrip and sentTripInvites values are used to collect the respective values from the
 *   StateFlow.
 * - isAlreadyAParticipant is used to assess if the user is already in the participant list of the
 *   selectedTrip.
 * - existingInviteId is used to identify the existing invite ID from the sentTripInvites value.
 * - isInvitePending assesses if there is a pending trip invite by checking if existingInviteId is
 *   not null (has been found).
 *
 * ## Parameters
 *
 * @param users A list of user pairs, where the first item is the user and the second item indicates
 *   whether the user is selected.
 * @param tripsViewModel The `TripsViewModel` instance for managing trip-related data.
 * @param tripId The ID of the trip for which the dropdown is used.
 * @param modifier The modifier to be applied to the dropdown container.
 * @param onRemove A callback invoked when a participant is removed. It provides the removed user
 *   pair and its index in the list.
 */
@Composable
fun UserDropdown(
    users: List<Pair<User, Boolean>>,
    tripsViewModel: TripsViewModel,
    tripId: String,
    modifier: Modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart),
    onRemove: (Pair<User, Boolean>, Int) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  val connectionStatus by connectivityState()
  val isConnected = connectionStatus == ConnectionState.Available

  // Collect the selected trip in case the user is in Edit Mode
  val selectedTrip by tripsViewModel.selectedTrip.collectAsState()

  val sentTripInvites by tripsViewModel.sentTripInvites.collectAsState()

  // Add an explicit null check for the selected trip
  if (selectedTrip == null) {
    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
      Text(
          text = stringResource(R.string.null_selected_trip_label),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.testTag("errorText"))
    }
    return
  } else {
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
            if (selectedUsers.isEmpty()) {
              Text(stringResource(R.string.participants_label))
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
          modifier = Modifier.fillMaxWidth().testTag("userDropDown")) {
            users.forEachIndexed { index, userPair ->
              val user = userPair.first
              val isAlreadyParticipant = selectedTrip!!.participants.contains(user.id)
              val existingInviteId =
                  sentTripInvites
                      .find { invite ->
                        invite.to == user.id && invite.tripId == (selectedTrip!!.id)
                      }
                      ?.id
              val isInvitePending = existingInviteId != null

              DropdownMenuItem(
                  text = { Text(user.name) },
                  onClick = { /* Do nothing, actions are on buttons */},
                  trailingIcon = {
                    when {
                      isAlreadyParticipant -> {
                        Button(
                            onClick = {
                              val updatedParticipants =
                                  selectedTrip!!.participants.filter { it != user.id }
                              val updatedTrip =
                                  selectedTrip!!.copy(participants = updatedParticipants)
                              updatedTrip.let {
                                tripsViewModel.updateTrip(it)
                                tripsViewModel.selectTrip(it)
                              }
                              onRemove(userPair, index)
                            },
                            modifier = Modifier.testTag("removeButton_${user.id}"),
                            enabled = isConnected,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error)) {
                              Text(stringResource(R.string.remove_label))
                            }
                      }
                      isInvitePending -> {
                        Button(
                            onClick = {
                              existingInviteId?.let { invite ->
                                tripsViewModel.declineTripInvite(invite)
                              }
                            },
                            modifier = Modifier.testTag("cancelButton_${user.id}"),
                            enabled = isConnected,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary)) {
                              Text(stringResource(R.string.cancel_label))
                            }
                      }
                      else -> {
                        Button(
                            onClick = { tripsViewModel.sendTripInvite(user.id, tripId) },
                            modifier = Modifier.testTag("inviteButton_${user.id}"),
                            enabled = isConnected,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary)) {
                              Text(stringResource(R.string.invite_label))
                            }
                      }
                    }
                  })
            }
          }
    }
  }
}

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.user.User
import com.android.voyageur.ui.profile.interests.InterestChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileContent(
    userData: User,
    showEditAndSignOutButtons: Boolean = false,
    isContactAdded: Boolean = false,
    onSignOut: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onAddOrRemoveContact: (() -> Unit)? = null
) {
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("userProfileContent"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        if (userData.profilePicture.isNotEmpty()) {
          Image(
              painter = rememberAsyncImagePainter(model = userData.profilePicture),
              contentDescription = "Profile Picture",
              modifier = Modifier.size(128.dp).clip(CircleShape).testTag("userProfilePicture"))
        } else {
          Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "Default Profile Picture",
              modifier = Modifier.size(128.dp).testTag("defaultProfilePicture"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userData.name.takeIf { it.isNotEmpty() } ?: "No name available",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("userName"))
        Text(
            text = userData.email.takeIf { it.isNotEmpty() } ?: "No email available",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("userEmail"))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Interests:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp))

        if (userData.interests.isNotEmpty()) {
          FlowRow(
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("interestsFlowRow"),
              horizontalArrangement = Arrangement.Center) {
                userData.interests.forEach { interest ->
                  InterestChip(interest = interest, modifier = Modifier.padding(4.dp))
                }
              }
        } else {
          Text(
              text = "No interests added yet",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("noInterests"))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
          if (showEditAndSignOutButtons && onEdit != null && onSignOut != null) {
            Button(onClick = onEdit, modifier = Modifier.testTag("editButton")) {
              Text(text = "Edit")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onSignOut, modifier = Modifier.testTag("signOutButton")) {
              Text(text = "Sign Out")
            }
          } else if (onAddOrRemoveContact != null) {
            Button(
                onClick = onAddOrRemoveContact,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isContactAdded) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("userProfileAddRemoveContactButton")) {
                  Text(text = if (isContactAdded) "Remove" else "Add")
                }
          }
        }
      }
}

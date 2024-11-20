import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.voyageur.model.user.User
import com.android.voyageur.ui.formFields.UserDropdown
import com.android.voyageur.ui.formFields.UserIcon
import org.junit.Rule
import org.junit.Test

class DropDownTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun userIcon_displaysProfilePic() {
    composeTestRule.setContent {
      UserIcon(User(name = "Alice", profilePicture = "https://test.com/profile.jpg"))
    }
    // Assert that the participant profile picture is displayed
    composeTestRule.onNodeWithTag("profilePic").assertExists()
  }

  // Tests for UserDropdown
  @Test
  fun userDropdown_displaysParticipants() {
    val users =
        listOf(Pair(User(name = "Alice", profilePicture = "https://test.com/profile.jpg"), true))

    composeTestRule.setContent { UserDropdown(users = users, onUpdate = { _, _ -> }) }
    // Assert that the participant profile picture is displayed
    composeTestRule.onNodeWithTag("profilePic", useUnmergedTree = true).assertExists()

    // Verify the label "Participants" does not exist because participants are selected
    composeTestRule.onNodeWithText("Participants").assertDoesNotExist()
  }
}

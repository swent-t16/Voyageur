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

  // Tests for UserIcon
  @Test
  fun userIcon_displaysCorrectInitial() {
    composeTestRule.setContent { UserIcon(text = "Alice") }
    // Assert that the participant avatar exists and shows the correct initial
    composeTestRule.onNodeWithTag("participantAvatar").assertExists()
    composeTestRule.onNodeWithText("A").assertExists()
  }

  // Tests for UserDropdown
  @Test
  fun userDropdown_displaysParticipants() {
    val users =
        listOf(
            Pair(User(name = "Alice"), true),
            Pair(User(name = "Bob"), false),
            Pair(User(name = "Charlie"), true))

    composeTestRule.setContent { UserDropdown(users = users, onUpdate = { _, _ -> }) }

    // Verify the initially selected participants are displayed
    composeTestRule.onNodeWithText("A").assertExists()
    composeTestRule.onNodeWithText("C").assertExists()

    // Verify the label "Participants" does not exist because participants are selected
    composeTestRule.onNodeWithText("Participants").assertDoesNotExist()
  }
}

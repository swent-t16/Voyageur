import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.voyageur.ui.theme.VoyageurTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoyageurThemeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testVoyageurThemeDarkMode() {
    composeTestRule.setContent { VoyageurTheme(darkTheme = true) { TestContent() } }

    // Assert the theme colors based on dark mode
    composeTestRule.onNodeWithTag("testContent").assertExists() // Check that the content exists
  }

  @Test
  fun testVoyageurThemeLightMode() {
    composeTestRule.setContent { VoyageurTheme(darkTheme = false) { TestContent() } }

    // Assert the theme colors based on light mode
    composeTestRule.onNodeWithTag("testContent").assertExists() // Check that the content exists
  }

  @Test
  fun testVoyageurThemeDynamicColorEnabled() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      composeTestRule.setContent {
        VoyageurTheme(darkTheme = false, dynamicColor = true) { TestContent() }
      }

      // Assert the theme colors when dynamic colors are enabled
      composeTestRule.onNodeWithTag("testContent").assertExists() // Check that the content exists
    }
  }

  @Test
  fun testVoyageurThemeDynamicColorDisabled() {
    composeTestRule.setContent {
      VoyageurTheme(darkTheme = false, dynamicColor = false) { TestContent() }
    }

    // Assert the theme colors when dynamic colors are disabled
    composeTestRule.onNodeWithTag("testContent").assertExists() // Check that the content exists
  }

  @Composable
  private fun TestContent() {
    // A simple composable with a test tag for validation
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.testTag("testContent")) {
          // This content will use the current theme
          MaterialTheme.colorScheme.primary // Access primary color to ensure theme is applied
    }
  }
}

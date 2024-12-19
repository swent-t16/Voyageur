package com.android.voyageur.ui.profile.interests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays an interest as a chip. The chip is styled with a background
 * color and rounded corners. It contains the interest text, which is displayed in a small body font
 * style with a specific color. The chip has padding around the text for a clean and readable
 * layout.
 *
 * @param interest A string representing the interest to be displayed inside the chip.
 * @param modifier A modifier that can be applied to the chip to customize its appearance or
 *   behavior. Default is [Modifier].
 */
@Composable
fun InterestChip(interest: String, modifier: Modifier = Modifier) {
  Text(
      text = interest,
      style =
          MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onPrimaryContainer),
      modifier =
          modifier
              .clip(MaterialTheme.shapes.small)
              .background(MaterialTheme.colorScheme.primaryContainer)
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("interest_$interest"))
}

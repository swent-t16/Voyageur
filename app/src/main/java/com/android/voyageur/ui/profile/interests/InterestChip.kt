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

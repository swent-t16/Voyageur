package com.android.voyageur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable that displays a "no results found" message with a search icon and helpful guidance
 * text. This component is designed to be shown when a search operation returns no results.
 *
 * The component includes:
 * - A search icon for visual feedback
 * - A main "No results found" message
 * - Additional guidance text suggesting how to adjust the search
 *
 * The component uses Material Design 3 theming and is styled with rounded corners and appropriate
 * spacing.
 *
 * Usage example:
 * ```
 * NoResultsFound(
 *     modifier = Modifier.testTag("noSearchResults")
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the component for test tags, if necessary
 */
@Composable
fun NoResultsFound(modifier: Modifier = Modifier) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = 16.dp, horizontal = 16.dp)
              .background(
                  MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
              .padding(24.dp), // Additional padding for spacing
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Icon for visual appeal
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "No results found",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(48.dp))

              Spacer(modifier = Modifier.height(16.dp))

              // Main message text
              Text(
                  text = "No results found",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              Spacer(modifier = Modifier.height(8.dp))

              // Additional guidance text
              Text(
                  text = "Try adjusting your search or check for typos.",
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
            }
      }
}

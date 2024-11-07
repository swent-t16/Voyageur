package com.android.voyageur.ui.profile.interests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun InterestChipEditable(interest: String, onRemove: () -> Unit) {
  var showDialog by remember { mutableStateOf(false) }

  // Chip UI
  Box(
      modifier =
          Modifier.padding(4.dp)
              .clip(MaterialTheme.shapes.small)
              .background(MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
              Text(
                  text = interest,
                  style =
                      MaterialTheme.typography.bodySmall.copy(
                          color = MaterialTheme.colorScheme.onPrimaryContainer))
              Spacer(modifier = Modifier.width(8.dp))
              IconButton(
                  onClick = { showDialog = true },
                  modifier = Modifier.size(16.dp).testTag("removeInterestButton_$interest")) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove Interest",
                        tint = MaterialTheme.colorScheme.error)
                  }
            }
      }

  // Confirmation Dialog
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(text = "Remove Interest") },
        text = { Text("Are you sure you want to remove \"$interest\" from your interests?") },
        confirmButton = {
          TextButton(
              onClick = {
                onRemove()
                showDialog = false
              }) {
                Text("Remove")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
  }
}

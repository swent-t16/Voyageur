package com.android.voyageur.ui.formFields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.voyageur.model.user.User

@Composable
fun UserIcon(text: String) {
  Box(
      modifier =
          Modifier.size(30.dp) // Set size for the avatar circle
              .testTag("participantAvatar")
              .background(Color.Gray, shape = RoundedCornerShape(50)), // Circular shape
      contentAlignment = Alignment.Center) {
        Text(text = text.first().uppercaseChar().toString(), color = Color.White)
      }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable()
fun UserDropdown(
    users: List<Pair<User, Boolean>>,
    modifier: Modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart),
    onUpdate: (Pair<User, Boolean>, Int) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

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
            Text("Participants")
          }
          selectedUsers.forEach {
            Column(modifier = Modifier.padding(4.dp).align(Alignment.CenterVertically)) {
              UserIcon(it.first.name)
            }
          }
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()) {
          users.forEachIndexed { index, userPair ->
            DropdownMenuItem(
                text = { Text("${userPair.first.name} ") },
                onClick = { onUpdate(userPair, index) },
                leadingIcon = {
                  Checkbox(
                      checked = userPair.second, onCheckedChange = { onUpdate(userPair, index) })
                })
          }
        }
  }
}

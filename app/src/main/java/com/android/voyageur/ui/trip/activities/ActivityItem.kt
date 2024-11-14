package com.android.voyageur.ui.trip.activities

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.model.activity.Activity
import com.android.voyageur.model.activity.hasDescription
import com.android.voyageur.model.activity.hasEndDate
import com.android.voyageur.model.activity.hasStartTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Composable that displays an activity item in a card view. Initially it shows the title and the
 * start and end time of the activity. It can be expanded to show the description, price and type of
 * the activity.
 */
@Composable
fun ActivityItem(
    activity: Activity,
) {
  var isExpanded by remember { mutableStateOf(false) }

  val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
  val timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

  // Convert Timestamp to LocalDateTime for formatting
  val startLocalDateTime =
      activity.startTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
  val endLocalDateTime =
      activity.endTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

  // Format the date and time
  val dateFormatted = dateFormat.format(startLocalDateTime)
  val startTimeFormatted = timeFormat.format(startLocalDateTime)
  val endTimeFormatted = timeFormat.format(endLocalDateTime)

  val context = LocalContext.current

  Card(
      shape = MaterialTheme.shapes.medium,
      modifier = Modifier.padding(start = 10.dp, end = 10.dp).testTag("cardItem_${activity.title}"),
      content = {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
          Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                      text = activity.title,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(end = 8.dp),
                      fontSize = 16.sp)
                  if (activity.hasStartTime() && activity.hasEndDate()) {
                    Text(
                        text = "$dateFormatted $startTimeFormatted - $endTimeFormatted",
                        fontSize = 12.sp,
                        color = Color.Gray)
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Delete icon, visible only when expanded
                  AnimatedVisibility(visible = isExpanded) {
                    IconButton(
                        onClick = { /*TODO: delete activity*/
                          Toast.makeText(
                                  context,
                                  "Delete activity not implemented yet",
                                  Toast.LENGTH_SHORT)
                              .show()
                        }) {
                          Icon(
                              imageVector = Icons.TwoTone.Delete,
                              contentDescription = "Delete Activity",
                              tint = Color.Red)
                        }
                  }

                  // Expand/collapse icon
                  IconButton(
                      onClick = { isExpanded = !isExpanded },
                      modifier = Modifier.testTag("expandIcon_${activity.title}")) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.rotate(if (isExpanded) 180f else 0f))
                      }
                }
              }

          // Expandable content with animation
          AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
              if (activity.hasDescription()) {
                Text(
                    text = "Description",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp))
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp))
              }
              // TODO: Add location once we have the final location model
              Text(
                  text = "Price",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(vertical = 2.dp))
              Text(
                  text = "${activity.estimatedPrice} CHF",
                  fontSize = 14.sp,
                  modifier = Modifier.padding(bottom = 8.dp))

              Text(
                  text = "Type",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  modifier = Modifier.padding(vertical = 2.dp))
              Text(text = activity.activityType.toString(), fontSize = 14.sp)
            }
          }
        }
      })
}

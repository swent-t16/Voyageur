package com.android.voyageur.ui.formFields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.voyageur.R
import java.util.Calendar

/**
 * A composable function that displays a modal dialog with a time picker, allowing users to select a
 * specific time.
 *
 * @param initialHour The initial hour to display in the time picker (24-hour format). If `null`,
 *   defaults to the current hour.
 * @param initialMinute The initial minute to display in the time picker. If `null`, defaults to the
 *   current minute.
 * @param onTimeSelected A lambda that is invoked when the user confirms their time selection. It
 *   receives the selected time as milliseconds since epoch, or `null` if no time is selected.
 * @param onDismiss A lambda that is invoked when the dialog is dismissed, either via the cancel
 *   button or by tapping outside the dialog.
 *
 * This composable uses Material3's `TimeInput` to provide an interactive time selection UI. It
 * wraps the picker inside a styled dialog with "OK" and "Cancel" buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerInput(
    initialHour: Int?,
    initialMinute: Int?,
    onTimeSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
  // Initialize the time picker state, defaulting to a 24-hour clock format
  val timeState = rememberTimePickerState(is24Hour = true)

  // Set the initial time values if provided
  if (initialHour != null && initialMinute != null) {
    timeState.hour = initialHour
    timeState.minute = initialMinute
  }

  // Display the dialog containing the time picker
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 10.dp,
        modifier = Modifier.padding(16.dp).testTag("timePickerDialog")) {
          Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    text = stringResource(R.string.select_time),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium)
                TimeInput(state = timeState)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
                  TextButton(
                      onClick = {
                        // Get the selected time in milliseconds since epoch
                        val selectedTime =
                            Calendar.getInstance()
                                .apply {
                                  set(Calendar.HOUR_OF_DAY, timeState.hour)
                                  set(Calendar.MINUTE, timeState.minute)
                                }
                                .timeInMillis
                        onTimeSelected(selectedTime) // Invoke the callback with the selected time
                      }) {
                        Text(text = stringResource(R.string.ok))
                      }
                }
              }
        }
  }
}

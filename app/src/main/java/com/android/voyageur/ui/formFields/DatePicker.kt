package com.android.voyageur.ui.formFields

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.voyageur.R

/**
 * A composable function that displays a modal dialog with a date picker, allowing users to select a
 * date.
 *
 * @param onDateSelected A lambda that is invoked when the user confirms their date selection. It
 *   receives the selected date in milliseconds since epoch, or `null` if no date is selected.
 * @param onDismiss A lambda that is invoked when the dialog is dismissed, either by the cancel
 *   button or by tapping outside the dialog.
 * @param selectedDate The initially selected date in milliseconds since epoch. Defaults to the
 *   current time.
 *
 * This function uses Material3's `DatePicker` and `DatePickerDialog` components to provide a modern
 * date selection UI. It includes "OK" and "Cancel" buttons for user interaction, and applies a test
 * tag for UI testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    selectedDate: Long = System.currentTimeMillis()
) {
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = {
              onDateSelected(datePickerState.selectedDateMillis)
              onDismiss()
            },
            modifier = Modifier.testTag("datePickerModal")) {
              Text(stringResource(R.string.ok))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
      }) {
        DatePicker(state = datePickerState)
      }
}

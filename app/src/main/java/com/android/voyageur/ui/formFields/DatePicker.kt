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
              Text("OK")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        DatePicker(state = datePickerState)
      }
}

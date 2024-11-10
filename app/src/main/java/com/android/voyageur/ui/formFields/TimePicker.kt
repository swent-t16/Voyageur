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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerInput(
    initialHour: Int?,
    initialMinute: Int?,
    onTimeSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
  val timeState = rememberTimePickerState(is24Hour = true)

  if (initialHour != null && initialMinute != null) {
    timeState.hour = initialHour
    timeState.minute = initialMinute
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 10.dp,
        modifier = Modifier.padding(16.dp)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                text = "Select time",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium)
            TimeInput(state = timeState)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              TextButton(onClick = onDismiss) { Text(text = "Cancel") }
              TextButton(
                  onClick = {
                    val selectedTime =
                        Calendar.getInstance()
                            .apply {
                              set(Calendar.HOUR_OF_DAY, timeState.hour)
                              set(Calendar.MINUTE, timeState.minute)
                            }
                            .timeInMillis
                    onTimeSelected(selectedTime)
                  }) {
                    Text(text = "OK")
                  }
            }
          }
        }
  }
}

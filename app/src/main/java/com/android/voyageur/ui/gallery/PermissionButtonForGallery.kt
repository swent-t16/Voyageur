package com.android.voyageur.ui.gallery

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun PermissionButtonForGallery(
    onUriSelected: (Uri?) -> Unit,
    messageToShow: String,
    dialogMessage: String,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showRationaleDialog by remember { mutableStateOf(false) }
  val permissionVersion =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        READ_MEDIA_IMAGES
      } else {
        READ_EXTERNAL_STORAGE
      }
  val galleryLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
          onUriSelected(uri)
        } else {
          Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
      }
  val permissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        when {
          isGranted -> galleryLauncher.launch("image/*")
          checkLimitedPermission(context) -> galleryLauncher.launch("image/*")
          else -> {
            // Permission is denied, show a toast to the user
            Toast.makeText(
                    context, "Permission denied. Unable to select photo.", Toast.LENGTH_SHORT)
                .show()
          }
        }
      }
  Button(
      onClick = {
        when {
          checkFullPermission(context) || checkLimitedPermission(context) -> {
            // If permission is granted, launch the gallery
            galleryLauncher.launch("image/*")
          }
          ActivityCompat.shouldShowRequestPermissionRationale(
              context.findActivity(), permissionVersion) -> {
            // Show rationale
            showRationaleDialog = true
          }
          else -> {
            permissionLauncher.launch(permissionVersion)
          }
        }
      },
      modifier = modifier) {
        // Shows message corresponding to the screen
        Text(messageToShow)
        // Show rationale dialog if needed
        if (showRationaleDialog) {
          AlertDialog(
              onDismissRequest = { showRationaleDialog = false },
              title = { Text("Permission Required") },
              text = { Text(dialogMessage) },
              confirmButton = {
                TextButton(
                    onClick = {
                      showRationaleDialog = false
                      permissionLauncher.launch(permissionVersion)
                    }) {
                      Text("Allow")
                    }
              },
              dismissButton = {
                TextButton(
                    onClick = {
                      showRationaleDialog = false
                      Toast.makeText(
                              context,
                              "Permission denied. Unable to select photo.",
                              Toast.LENGTH_SHORT)
                          .show()
                    }) {
                      Text("Cancel")
                    }
              })
        }
      }
}

fun checkFullPermission(context: Context): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
  } else {
    ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
  }
}

fun checkLimitedPermission(context: Context): Boolean {
  return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
      ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) ==
          PERMISSION_GRANTED)
}
/**
 * Return Component Activity associated with Context to show dialog. Unwraps context on which it is
 * called upon.
 * *
 */
fun Context.findActivity(): ComponentActivity {
  var context = this
  while (context is ContextWrapper) {
    if (context is ComponentActivity) {
      return context
    }
    context = context.baseContext
  }
  throw IllegalStateException("No Activity found")
}

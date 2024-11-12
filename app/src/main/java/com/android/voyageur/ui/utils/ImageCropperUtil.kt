package com.android.voyageur.ui.utils

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

/**
 * Represents the result of an image cropping operation.
 *
 * @property imageUri The URI of the cropped image as a string, or null if cropping failed
 * @property error Error message if the cropping operation failed, or null if successful
 */
data class ImageCropperResult(val imageUri: String?, val error: String?)

/**
 * A composable function that provides image cropping functionality with gallery integration. This
 * utility allows users to either crop an existing image or select one from the gallery and then
 * crop it.
 *
 * Features:
 * - Customizable aspect ratio
 * - Gallery integration
 * - Error handling with user feedback
 * - JPEG compression with quality control
 * - Cropping guidelines
 *
 * Usage example:
 * ```
 * val imageCropper = rememberImageCropper(
 *     aspectRatioX = 1,
 *     aspectRatioY = 1
 * ) { result ->
 *     result.imageUri?.let { uri ->
 *         // Handle the cropped image URI
 *     }
 *     result.error?.let { error ->
 *         // Handle any errors
 *     }
 * }
 *
 * // Launch with existing URI
 * imageCropper(existingUri)
 *
 * // Or launch gallery picker
 * imageCropper(null)
 * ```
 *
 * @param aspectRatioX The horizontal aspect ratio for cropping (default: 16)
 * @param aspectRatioY The vertical aspect ratio for cropping (default: 9)
 * @param onResult Callback function that receives the [ImageCropperResult] after cropping
 * @return A function that accepts a nullable [Uri] to either crop an existing image or launch the
 *   gallery picker
 */
@Composable
fun rememberImageCropper(
    aspectRatioX: Int = 16,
    aspectRatioY: Int = 9,
    onResult: (ImageCropperResult) -> Unit
): (Uri?) -> Unit {
  val context = LocalContext.current

  // Initialize the image cropper launcher
  val cropImage =
      rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
          onResult(ImageCropperResult(result.uriContent?.toString(), null))
        } else {
          val error = result.error?.message ?: "Image cropping failed"
          onResult(ImageCropperResult(null, error))
          Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
      }

  // Initialize the gallery picker launcher
  val galleryLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
          val cropOptions =
              CropImageContractOptions(
                  uri = it,
                  cropImageOptions =
                      CropImageOptions(
                          imageSourceIncludeGallery = true,
                          imageSourceIncludeCamera = false,
                          aspectRatioX = aspectRatioX,
                          aspectRatioY = aspectRatioY,
                          fixAspectRatio = true,
                          guidelines = CropImageView.Guidelines.ON,
                          outputCompressFormat = Bitmap.CompressFormat.JPEG,
                          outputCompressQuality = 90))
          cropImage.launch(cropOptions)
        }
      }

  // Return a remembered callback that handles both direct URI cropping and gallery picking
  return remember {
    { uri: Uri? ->
      if (uri != null) {
        val cropOptions =
            CropImageContractOptions(
                uri = uri,
                cropImageOptions =
                    CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = false,
                        aspectRatioX = aspectRatioX,
                        aspectRatioY = aspectRatioY,
                        fixAspectRatio = true,
                        guidelines = CropImageView.Guidelines.ON,
                        outputCompressFormat = Bitmap.CompressFormat.JPEG,
                        outputCompressQuality = 90))
        cropImage.launch(cropOptions)
      } else {
        galleryLauncher.launch("image/*")
      }
    }
  }
}

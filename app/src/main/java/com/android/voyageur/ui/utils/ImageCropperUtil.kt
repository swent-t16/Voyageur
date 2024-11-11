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

data class ImageCropperResult(
    val imageUri: String?,
    val error: String?
)

@Composable
fun rememberImageCropper(
    aspectRatioX: Int = 16,
    aspectRatioY: Int = 9,
    onResult: (ImageCropperResult) -> Unit
): (Uri?) -> Unit {
    val context = LocalContext.current

    val cropImage = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            onResult(ImageCropperResult(result.uriContent?.toString(), null))
        } else {
            val error = result.error?.message ?: "Image cropping failed"
            onResult(ImageCropperResult(null, error))
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val cropOptions = CropImageContractOptions(
                uri = it,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    imageSourceIncludeCamera = false,
                    aspectRatioX = aspectRatioX,
                    aspectRatioY = aspectRatioY,
                    fixAspectRatio = true,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.JPEG,
                    outputCompressQuality = 90
                )
            )
            cropImage.launch(cropOptions)
        }
    }

    return remember {
        { uri: Uri? ->
            if (uri != null) {
                val cropOptions = CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = false,
                        aspectRatioX = aspectRatioX,
                        aspectRatioY = aspectRatioY,
                        fixAspectRatio = true,
                        guidelines = CropImageView.Guidelines.ON,
                        outputCompressFormat = Bitmap.CompressFormat.JPEG,
                        outputCompressQuality = 90
                    )
                )
                cropImage.launch(cropOptions)
            } else {
                galleryLauncher.launch("image/*")
            }
        }
    }
}
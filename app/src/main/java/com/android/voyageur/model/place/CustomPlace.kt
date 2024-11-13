package com.android.voyageur.model.place

import androidx.compose.ui.graphics.ImageBitmap
import com.google.android.libraries.places.api.model.Place

data class CustomPlace(
    val place: Place,
    val photos: List<ImageBitmap>,
)

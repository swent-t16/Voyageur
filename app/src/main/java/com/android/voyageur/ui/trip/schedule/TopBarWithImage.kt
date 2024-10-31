package com.android.voyageur.ui.trip.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.R
import com.android.voyageur.model.trip.Trip
import com.android.voyageur.ui.navigation.NavigationActions
import com.android.voyageur.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithImage(selectedTrip: Trip, navigationActions: NavigationActions) {
  Box(modifier = Modifier.fillMaxWidth().height(145.dp)) {
    // Background Image
    if (selectedTrip.imageUri.isNotEmpty()) {
      Image(
          painter = rememberAsyncImagePainter(model = selectedTrip.imageUri),
          contentDescription = "Selected image",
          contentScale = ContentScale.Crop,
          modifier =
              Modifier.fillMaxSize() // Fill the entire Box area
                  .testTag("tripImage"))
    } else {
      Image(
          painter = painterResource(id = R.drawable.default_trip_image),
          contentDescription = "Default trip image",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().testTag("defaultTripImage"))
    }

    // Centered Text Overlay with Rounded Rectangle Background
    Box(
        modifier =
            Modifier.align(Alignment.Center) // Center overlay in the Box
                .clip(RoundedCornerShape(20.dp))
                .width(279.dp)
                .height(72.dp)
                .background(Color.White.copy(alpha = 0.7f))
                // .padding(horizontal = 77.dp, vertical = 11.dp)
                .wrapContentSize()
                .widthIn(max = 250.dp) // Maximum width available
        ) {
          Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                Text(
                    text = selectedTrip.name,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = Color.Black,
                )
                Text(
                    // TODO: Replace with correct date format
                    text =
                        selectedTrip.startDate.toDateWithoutYearString() +
                            " - " +
                            selectedTrip.endDate.toDateWithYearString(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Black)
              }
        }

    // TopAppBar with Transparent Background
    TopAppBar(
        title = {},
        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
          // Back Button with Circular White Background
          IconButton(
              onClick = { navigationActions.navigateTo(Screen.OVERVIEW) },
              modifier =
                  Modifier.padding(8.dp)
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(Color.White.copy(alpha = 0.7f))) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Home",
                    tint = Color.Black)
              }
        })
  }
}

fun Timestamp.toDateWithoutYearString(): String {
  // TODO: Test this with 2 digit dates (I am not sure it works)
  val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

fun Timestamp.toDateWithYearString(): String {
  val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
  return sdf.format(this.toDate())
}

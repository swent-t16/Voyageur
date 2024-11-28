package com.android.voyageur.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.voyageur.R
import com.android.voyageur.model.place.CustomPlace
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.ui.navigation.NavigationActions
import kotlinx.coroutines.launch

/**
 * Composable function that displays the details of a selected place.
 *
 * @param navigationActions Actions to handle navigation events.
 * @param placesViewModel ViewModel that holds the state of the selected place.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailsScreen(navigationActions: NavigationActions, placesViewModel: PlacesViewModel) {
  val customPlace by placesViewModel.selectedPlace.collectAsState()
  val isLoading by placesViewModel.isLoading.collectAsState()
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Place Details") },
            navigationIcon = {
              IconButton(
                  onClick = {
                    placesViewModel.deselectPlace()
                    navigationActions.goBack()
                  }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.testTag("BackButton"))
                  }
            })
      },
      content = { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.TopCenter) {
              if (isLoading) {
                // Display a loading indicator while fetching place details
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(48.dp).testTag("LoadingIndicator").align(Alignment.Center))
              } else
                  Column(
                      modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        customPlace?.let { PlaceDetailsContent(customPlace = it) }
                      }
            }
      })
}

/**
 * Composable function that displays the content of the place details.
 *
 * @param customPlace The place to display details for.
 */
@Composable
fun PlaceDetailsContent(customPlace: CustomPlace) {
  val context = LocalContext.current

  Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("PlaceDetailsContent"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        val place = customPlace.place
        place.name?.let {
          Text(
              text = it,
              style =
                  MaterialTheme.typography.headlineMedium.copy(
                      fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
              modifier = Modifier.padding(bottom = 8.dp))
        }
        place.address?.let {
          Text(
              text = it,
              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
              modifier = Modifier.padding(bottom = 8.dp))
        }

        PhotoCarousel(customPlace = customPlace)

        // Display rating and number of ratings
        if (place.rating != null && place.userRatingsTotal != null) {
          Text(
              text = "Rating: ${place.rating} ★ (${place.userRatingsTotal} reviews)",
              style =
                  MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.secondary),
              modifier = Modifier.padding(bottom = 8.dp).testTag("RatingText"))
        } else {
          Text(
              text = "No ratings available",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
              modifier = Modifier.padding(bottom = 8.dp).testTag("NoRatingsText"))
        }

        Text(
            text =
                if (place.priceLevel != null) "Price Level: ${"$".repeat(place.priceLevel)}"
                else "Price Level: N/A",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
            modifier = Modifier.padding(bottom = 8.dp).testTag("PriceLevelText"))
        Text(
            text = "Phone: ${place.internationalPhoneNumber ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
            modifier = Modifier.padding(bottom = 8.dp).testTag("PhoneNumberText"))

        place.openingHours?.let { OpeningHoursWidget(openingHours = it.weekdayText) }
            ?: run {
              Text(
                  text = "Opening hours not available",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
                  modifier = Modifier.padding(bottom = 8.dp).testTag("NoOpeningHoursText"))
            }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                  onClick = {
                    if (place.websiteUri != null) {
                      val intent = Intent(Intent.ACTION_VIEW, place.websiteUri)
                      context.startActivity(intent)
                    } else {
                      Toast.makeText(context, "Website not available", Toast.LENGTH_SHORT).show()
                    }
                  },
                  modifier = Modifier.weight(1f).testTag("LearnMoreButton")) {
                    Text(text = "Learn More", style = MaterialTheme.typography.labelLarge)
                  }

              Button(
                  onClick = {
                    if (place.latLng == null) {
                      Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                    } else {
                      val gmmIntentUri =
                          Uri.parse(
                              "geo:0,0?q=${place.latLng.latitude},${place.latLng.longitude}(${place.name})")
                      val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                      mapIntent.setPackage("com.google.android.apps.maps")
                      context.startActivity(mapIntent)
                    }
                  },
                  modifier = Modifier.weight(1f).testTag("GetDirectionsButton")) {
                    Text(text = "Get Directions")
                  }
            }
      }
}

/**
 * Composable function that displays the opening hours of a place.
 *
 * @param openingHours List of opening hours for each day of the week.
 */
@Composable
fun OpeningHoursWidget(openingHours: List<String>) {
  var selectedDayIndex by remember { mutableStateOf(0) }
  val daysOfWeek =
      listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

  Column(
      modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().testTag("OpeningHoursWidget"),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Opening Hours",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.padding(bottom = 8.dp))

        // Horizontal list of days
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(horizontal = 16.dp)) {
              items(daysOfWeek.size) { index ->
                val isSelected = index == selectedDayIndex
                Text(
                    text = daysOfWeek[index],
                    modifier =
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("DayText$index")
                            .background(
                                color =
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else Color.Transparent,
                                shape = RoundedCornerShape(12.dp))
                            .clickable { selectedDayIndex = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp)
              }
            }

        // Display the selected day's hours
        if (selectedDayIndex >= 0 && selectedDayIndex < openingHours.size) {
          Text(
              text = openingHours[selectedDayIndex],
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
              modifier = Modifier.padding(top = 8.dp).testTag("OpeningHoursText"))
        }
      }
}

/**
 * Composable function that displays a photo carousel for a place.
 *
 * @param customPlace The place containing the photos to display.
 */
@Composable
fun PhotoCarousel(customPlace: CustomPlace) {
  val coroutineScope = rememberCoroutineScope()
  // Use a state variable to observe changes in photos
  var photos by remember { mutableStateOf(customPlace.photos) }

  // Use a LaunchedEffect to react to changes in the photos list
  LaunchedEffect(customPlace.photos) {
    photos = emptyList()
    photos = customPlace.photos
  }

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(350.dp) // Adjust height as needed
              .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
              .testTag("PhotoCarousel")) {
        if (photos.isNotEmpty()) {
          // HorizontalPager for images when available
          val pagerState = rememberPagerState(pageCount = { photos.size })
          HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Image(
                bitmap = photos[page],
                contentDescription = "Place photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().testTag("PlacePhoto$page"))
          }

          // Navigation arrows
          IconButton(
              onClick = {
                coroutineScope.launch {
                  if (pagerState.currentPage > 0) {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                  }
                }
              },
              modifier =
                  Modifier.align(Alignment.CenterStart)
                      .padding(16.dp)
                      .testTag("PreviousImageButton")) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous image",
                    tint = Color.White.copy(alpha = 0.8f))
              }

          IconButton(
              onClick = {
                coroutineScope.launch {
                  if (pagerState.currentPage < pagerState.pageCount - 1) {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                  }
                }
              },
              modifier =
                  Modifier.align(Alignment.CenterEnd).padding(16.dp).testTag("NextImageButton")) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next image",
                    tint = Color.White.copy(alpha = 0.8f))
              }
        } else {
          // Fallback image when there are no photos
          Image(
              painter = painterResource(id = R.drawable.image_not_found),
              contentDescription = "Placeholder image",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().testTag("PlaceholderImage"))
        }
      }
}

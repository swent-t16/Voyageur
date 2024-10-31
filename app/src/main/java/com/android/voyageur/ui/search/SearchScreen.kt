package com.android.voyageur.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.voyageur.model.place.PlacesViewModel
import com.android.voyageur.model.user.User
import com.android.voyageur.model.user.UserViewModel
import com.android.voyageur.ui.navigation.BottomNavigationMenu
import com.android.voyageur.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.voyageur.ui.navigation.NavigationActions
import com.google.android.libraries.places.api.model.Place

@Composable
fun SearchScreen(
    userViewModel: UserViewModel,
    placesViewModel: PlacesViewModel,
    navigationActions: NavigationActions,
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  var selectedFilter by remember { mutableStateOf(FilterType.USERS) }
  var showFilters by remember { mutableStateOf(false) }
  val searchedUsers by userViewModel.searchedUsers.collectAsState()
  val searchedPlaces by placesViewModel.searchedPlaces.collectAsState()

  // make the search query call when the screen is first launched
  userViewModel.searchUsers(searchQuery.text)
  placesViewModel.searchPlaces(searchQuery.text)

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Column(
            modifier =
                Modifier.padding(pd)
                    .fillMaxSize()
                    .background(Color.White)
                    .testTag("searchScreenContent")) {
              Spacer(modifier = Modifier.height(24.dp))
              Text(
                  text = "Search",
                  color = Color.Black,
                  style = MaterialTheme.typography.bodyLarge,
                  fontSize = 24.sp,
                  modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))

              Row(
                  modifier =
                      Modifier.padding(horizontal = 16.dp)
                          .fillMaxWidth()
                          .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
                          .padding(8.dp)
                          .testTag("searchBar"),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                          searchQuery = it
                          userViewModel.setQuery(searchQuery.text)
                          placesViewModel.setQuery(searchQuery.text)
                        },
                        modifier = Modifier.weight(1f).padding(8.dp).testTag("searchTextField"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                    )
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Icon to show filters once clicked",
                        modifier =
                            Modifier.clickable { showFilters = !showFilters }.testTag("filterIcon"))
                  }
              if (showFilters) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("filterRow"),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                      FilterButton("Users", selectedFilter == FilterType.USERS) {
                        selectedFilter = FilterType.USERS
                      }
                      FilterButton("Locations", selectedFilter == FilterType.PLACES) {
                        selectedFilter = FilterType.PLACES
                      }
                      FilterButton("All", selectedFilter == FilterType.ALL) {
                        selectedFilter = FilterType.ALL
                      }
                    }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                  text = "Search result",
                  color = Color.Black,
                  fontSize = 18.sp,
                  modifier = Modifier.padding(horizontal = 16.dp))

              LazyColumn(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(16.dp)
                          .background(Color.LightGray, shape = MaterialTheme.shapes.large)
                          .testTag("searchResults")) {
                    // Display no results found message if searchedUsers is empty
                    // Will consider locations once Places API is integrated
                    when (selectedFilter) {
                      FilterType.USERS -> {
                        if (searchedUsers.isEmpty()) {
                          item {
                            Text(
                                text = "No results found",
                                color = Color.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(16.dp).testTag("noResultsFound"))
                          }
                        } else {
                          items(searchedUsers) { user ->
                            UserSearchResultItem(
                                user, Modifier.testTag("userItem_${user.id}"), userViewModel)
                          }
                        }
                      }
                      FilterType.PLACES -> {
                        if (searchedPlaces.isEmpty()) {
                          item {
                            Text(
                                text = "No results found",
                                color = Color.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(16.dp).testTag("noResultsFound"))
                          }
                        } else {
                          items(searchedPlaces) { place ->
                            PlaceSearchResultItem(place, Modifier.testTag("placeItem_${place.id}"))
                          }
                        }
                      }
                      FilterType.ALL -> {
                        item {
                          Text(
                              text = "Not yet implemented",
                              color = Color.Black,
                              fontSize = 18.sp,
                              modifier = Modifier.padding(16.dp).testTag("noResultsFound"))
                        }
                      }
                    }
                  }
            }
      })
}

@Composable
fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors =
          ButtonDefaults.buttonColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
              contentColor = Color.White),
      modifier = Modifier.padding(8.dp).testTag("filterButton_$label")) {
        Text(text = label)
      }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSearchResultItem(user: User, modifier: Modifier = Modifier, userViewModel: UserViewModel) {
  Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween) {
        FlowRow() {
          Image(
              painter = rememberAsyncImagePainter(model = user.profilePicture),
              contentDescription = "${user.name}'s profile picture",
              modifier =
                  Modifier.size(60.dp)
                      .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                      .clip(CircleShape)
                      .testTag("userProfilePicture_${user.id}"))
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
                text = user.name,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.testTag("userName_${user.id}"))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "@${user.username}",
                color = Color.Black,
                modifier = Modifier.testTag("userName_${user.id}"))
          }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = { userViewModel.addContact(user.id) },
            enabled =
                userViewModel.user.collectAsState().value?.contacts?.contains(user.id) == false) {
              Text("Add to contacts")
            }
      }
}

@Composable
fun PlaceSearchResultItem(place: Place, modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          // Place name
          Text(
              text = place.displayName ?: "Unknown Place",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.Black)

          Spacer(modifier = Modifier.height(4.dp))

          // Rating and review count
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${place.rating} ", fontSize = 14.sp, color = Color.Black)
            Text(
                text = "★".repeat(place.rating?.toInt() ?: 0),
                fontSize = 14.sp,
                color = Color(0xFFFFA000) // Orange for star rating
                )
            Text(text = " (${place.userRatingsTotal})", fontSize = 14.sp, color = Color.Gray)
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Price level, type, and address
          Text(
              text = "${"$".repeat(place.priceLevel ?: 1)} · ${place.address ?: "No address"}",
              fontSize = 14.sp,
              color = Color.Gray)

          Spacer(modifier = Modifier.height(4.dp))

          // Opening status and closing time
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (place.openingHours != null) {
              Text(
                  text =
                      " · Opening hours: ${place.openingHours?.periods?.firstOrNull()?.open?.time?.hours.toString() ?: "Unknown"} - ${place.openingHours?.periods?.firstOrNull()?.close?.time?.hours.toString() ?: "Unknown"}",
                  fontSize = 14.sp,
                  color = Color.Gray)
            }
          }
        }
      }
}

enum class FilterType {
  USERS,
  PLACES,
  ALL
}

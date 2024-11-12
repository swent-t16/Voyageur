package com.android.voyageur.model.place

import com.google.android.gms.maps.model.LatLng

interface PlacesRepository {
  fun searchPlaces(
      query: String,
      location: LatLng?,
      onSuccess: (List<CustomPlace>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}

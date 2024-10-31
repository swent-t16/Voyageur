package com.android.voyageur.model.place

import com.google.android.libraries.places.api.model.Place

interface PlacesRepository {
  fun searchPlaces(query: String, onSuccess: (List<Place>) -> Unit, onFailure: (Exception) -> Unit)
}

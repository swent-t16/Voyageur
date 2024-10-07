package com.android.voyageur.model.trip

import com.android.voyageur.model.location.Location
import com.google.firebase.Timestamp

data class Trip(
    val id: String,
    val creator: String,
    val participants: Array<String>,
    val description: String,
    val name: String,
    val location: Location,
    val startDate: Timestamp,
    val endDate: Timestamp,
    val activities: Array<Any>, // TODO : replace this with activity model
)

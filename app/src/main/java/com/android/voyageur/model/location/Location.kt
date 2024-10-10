package com.android.voyageur.model.location

data class Location(
    val country: String = "",
    val city: String = "",
    val county: String? = "",
    val zip: String? = "",
)

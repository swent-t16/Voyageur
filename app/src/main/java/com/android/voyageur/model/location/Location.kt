package com.android.voyageur.model.location

/**
 * Represents a geographical location with an identifier, name, address, and coordinates.
 *
 * @property id A unique identifier for the location. Defaults to an empty string.
 * @property name The name of the location (e.g., "Eiffel Tower"). Defaults to an empty string.
 * @property address The address of the location (e.g., "Champ de Mars, Paris, France"). Defaults to
 *   an empty string.
 * @property lat The latitude coordinate of the location in decimal degrees. Defaults to 0.0.
 * @property lng The longitude coordinate of the location in decimal degrees. Defaults to 0.0.
 */
data class Location(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

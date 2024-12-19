package com.android.voyageur.model.user

/**
 * Data class representing a user in the application.
 *
 * @property id The unique identifier for the user.
 * @property name The user's full name.
 * @property email The user's email address.
 * @property profilePicture The URL or path to the user's profile picture.
 * @property bio A short biography or description of the user.
 * @property contacts A list of the user's contacts, represented by their IDs.
 * @property interests A list of the user's interests (e.g., hobbies, topics of interest).
 * @property username The user's username used within the app.
 * @property favoriteTrips A list of trip IDs representing the user's favorite trips.
 * @property fcmToken Firebase Cloud Messaging token for push notifications (optional).
 */
data class User(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var profilePicture: String = "",
    var bio: String = "",
    var contacts: List<String> = mutableListOf(),
    var interests: List<String> = mutableListOf(),
    var username: String = "",
    val favoriteTrips: List<String> = emptyList(),
    var fcmToken: String? = null // Add FCM token with null default
) {

    /**
     * Compares this user object to another object for equality.
     *
     * @param other The other object to compare this user to.
     * @return `true` if the other object is a `User` and all properties are equal, `false` otherwise.
     */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (id != other.id) return false
    if (name != other.name) return false
    if (email != other.email) return false
    if (profilePicture != other.profilePicture) return false
    if (bio != other.bio) return false
    if (!contacts.containsAll(other.contacts) || !other.contacts.containsAll(contacts)) return false
    if (!interests.containsAll(other.interests) || !other.interests.containsAll(interests))
        return false
    if (username != other.username) return false
    if (!favoriteTrips.containsAll(other.favoriteTrips) ||
        !other.favoriteTrips.containsAll(favoriteTrips))
        return false
    if (fcmToken != other.fcmToken) return false // Add FCM token comparison

    return true
  }

    /**
     * Returns a hash code value for this user.
     *
     * The hash code is generated based on the user's properties to ensure that two equal `User` objects have the same hash code.
     *
     * @return A hash code for this user.
     */
  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + profilePicture.hashCode()
    result = 31 * result + bio.hashCode()
    result = 31 * result + contacts.toSet().hashCode()
    result = 31 * result + interests.toSet().hashCode()
    result = 31 * result + username.hashCode()
    result = 31 * result + favoriteTrips.toSet().hashCode()
    result = 31 * result + (fcmToken?.hashCode() ?: 0) // Add FCM token to hash
    return result
  }

    /**
     * Converts this `User` object to a map suitable for storing in Firestore.
     *
     * @return A map containing key-value pairs representing the properties of the `User` object.
     */
  fun toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "profilePicture" to profilePicture,
        "bio" to bio,
        "contacts" to contacts,
        "interests" to interests,
        "username" to username,
        "favoriteTrips" to favoriteTrips,
        "fcmToken" to fcmToken)
  }
}

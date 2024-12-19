package com.android.voyageur.model.user

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

  // Helper function to convert User to Map for Firestore
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

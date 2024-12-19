package com.android.voyageur.model.notifications

/**
 * Represents a friend request between two users within the application.
 *
 * @property id A unique identifier for the friend request. Defaults to an empty string.
 * @property from The ID of the user who sent the friend request. Defaults to an empty string.
 * @property to The ID of the user who is the recipient of the friend request. Defaults to an empty string.
 * @property accepted Indicates whether the friend request has been accepted. Defaults to `false`.
 *
 */
data class FriendRequest(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val accepted: Boolean = false
)

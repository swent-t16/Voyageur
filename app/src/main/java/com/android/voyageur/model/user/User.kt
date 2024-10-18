package com.android.voyageur.model.user

data class User(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var profilePicture: String = "",
    var bio: String = "",
    var contacts: List<String> = mutableListOf(),
    var username: String = ""
)

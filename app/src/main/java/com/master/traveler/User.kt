package com.master.traveler

data class User(
    val login: String,
    val username: String,
    val bio: String,
    val profilePicture: String,
    val nbFollowers: Int,
    val nbFollowing: Int
)

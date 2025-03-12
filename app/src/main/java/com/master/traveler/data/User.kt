package com.master.traveler.data

data class User(
    val id: String,
    val login: String,
    val username: String,
    val bio: String,
    val profilePicture: String,
    val nbFollowers: Int,
    val nbFollowing: Int,
    val posts_liked: List<String>,
    val posts_saved: List<String>,
    val followers: List<String>,
    val following: List<String>,
    val travels: Int
)

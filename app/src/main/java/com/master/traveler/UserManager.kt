package com.master.traveler

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.master.traveler.data.User

class UserManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
        val userJson = Gson().toJson(user)
        editor.putString("user_data", userJson)
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString("user_data", null)
        return if (userJson != null) Gson().fromJson(userJson, User::class.java) else null
    }

    fun clearUser() {
        sharedPreferences.edit().remove("user_data").apply()
    }

    fun toggleLikePost(postId: String) {
        val user = getUser()
        if (user != null) {
            val updatedPostsLiked = if (user.posts_liked.contains(postId)) {
                user.posts_liked - postId
            } else {
                user.posts_liked + postId
            }

            val updatedUser = user.copy(posts_liked = updatedPostsLiked)
            saveUser(updatedUser)
        }
    }

    fun toggleSavePost(postId: String) {
        val user = getUser()
        if (user != null) {
            val updatedPostsSaved = if (user.posts_saved.contains(postId)) {
                user.posts_saved - postId
            } else {
                user.posts_saved + postId
            }

            val updatedUser = user.copy(posts_saved = updatedPostsSaved)
            saveUser(updatedUser)
        }
    }

    fun setLogin(login: String) {
        val user = getUser()
        if (user != null) {
            val updatedUser = user.copy(login = login)
            saveUser(updatedUser)
        }
    }

    fun setUsername(username: String) {
        val user = getUser()
        if (user != null) {
            val updatedUser = user.copy(username = username)
            saveUser(updatedUser)
        }
    }

    fun toggleFollowUser(userId: String) {
        val user = getUser()
        if (user != null) {
            val isFollowing = user.following.contains(userId)

            val updatedFollowing = if (isFollowing) {
                user.following - userId
            } else {
                user.following + userId
            }

            val updatedNbFollowing = if (isFollowing) user.nbFollowing - 1 else user.nbFollowing + 1
            val updatedNbFollowers = if (isFollowing) user.nbFollowers - 1 else user.nbFollowers + 1

            val updatedUser = user.copy(
                following = updatedFollowing,
                nbFollowing = updatedNbFollowing,
                nbFollowers = updatedNbFollowers
            )

            saveUser(updatedUser)
        }
    }

    fun setBio(bio: String) {
        val user = getUser()
        if (user != null) {
            val updatedUser = user.copy(bio = bio)
            saveUser(updatedUser)
        }
    }
}
package com.master.traveler

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

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
}

package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import okhttp3.*
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private val posts = mutableListOf<PostHome>() // Liste des posts

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Récupération de l'utilisateur connecté
        val userManager = UserManager(this@HomeActivity)
        user = userManager.getUser() ?: run {
            // Si l'utilisateur n'est pas trouvé, peut-être rediriger vers la page de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }


        // Initialiser le RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Passer 'user' et la fonction onLikeClicked dans le constructeur de l'adapter
        postAdapter = PostAdapter(posts, user)
        recyclerView.adapter = postAdapter

        // Charger les posts depuis l'API
        fetchPostsFromApi()
    }

    private fun fetchPostsFromApi() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/posts") // Remplace avec l'URL correcte
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Gérer l'erreur
                Log.e("HomeActivity", "Erreur de réseau: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val gson = Gson()
                    val type = object : TypeToken<ApiResponse>() {}.type
                    val apiResponse: ApiResponse = gson.fromJson(jsonString, type)

                    runOnUiThread {
                        posts.clear()
                        posts.addAll(apiResponse.posts)
                        postAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}

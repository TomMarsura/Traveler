package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        val profileImageView = findViewById<ImageView>(R.id.profileIcon)

        // Charger l'image de profil avec Glide
        user.profilePicture?.let { profileUrl ->
            Glide.with(this)
                .load(profileUrl)
                .placeholder(R.drawable.ic_launcher_background) // Image par défaut en attendant le chargement
                .error(R.drawable.ic_launcher_foreground)             // Image en cas d'erreur
                .circleCrop()                                   // Pour afficher l'image en cercle
                .into(profileImageView)
        }

        // Bouton Maison : Remonte et recharge la page
        findViewById<ImageView>(R.id.buttonHome).setOnClickListener {
            recyclerView.scrollToPosition(0)
            fetchPostsFromApi() // Recharge les posts
        }

        // Filtrer par "Fil d'actualité"
        findViewById<TextView>(R.id.textFeed).setOnClickListener {
            fetchPostsFromApi() // Afficher les posts généraux
        }

        // Filtrer par "Suivis"
        findViewById<TextView>(R.id.textFollowing).setOnClickListener {
            // fetchFollowedPosts() // À décommenter lorsque cette fonction est prête
        }

        // Icône de Profil : Aller à l'activité Profil
        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfileOtherActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
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

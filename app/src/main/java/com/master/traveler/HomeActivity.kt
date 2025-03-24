package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        val textFeedButton: TextView = findViewById(R.id.textFeed)
        val textFollowedButton: TextView = findViewById(R.id.textFollowing)

        // Filtrer par "Fil d'actualité"
        textFeedButton.setOnClickListener {
            fetchPostsFromApi() // Afficher les posts généraux
            textFeedButton.setTextColor("#FFFFFF".toColorInt())
            textFollowedButton.setTextColor("#878787".toColorInt())
        }

        // Filtrer par "Suivis"
        textFollowedButton.setOnClickListener {
            fetchPostsFollowedFromApi() // Afficher les posts des utilisateurs suivis
            textFollowedButton.setTextColor("#FFFFFF".toColorInt())
            textFeedButton.setTextColor("#878787".toColorInt())
        }

        // Icône de Profil : Aller à l'activité Profil
        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfileOtherActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
            finish()
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

    private fun fetchPostsFollowedFromApi() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getFollowedPosts(user.id)

                if(response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()

                    launch(Dispatchers.Main) {
                        posts.clear()
                        posts.addAll(apiResponse!!.posts)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Log.e("HomeActivity", "Erreur lors de la récupération des posts : ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Log.e("HomeActivity", "Erreur réseau: ${e.message}")
                }
            }
        }
    }

    private fun fetchPostsFromApi() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getPosts()

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()

                    launch(Dispatchers.Main) {
                        posts.clear()
                        posts.addAll(apiResponse!!.posts)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Log.e("HomeActivity", "Erreur lors de la récupération des posts : ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Log.e("HomeActivity", "Erreur réseau: ${e.message}")
                }
            }
        }
    }
}

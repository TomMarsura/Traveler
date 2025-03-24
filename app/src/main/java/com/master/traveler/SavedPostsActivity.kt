package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class SavedPostsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val savedPosts = mutableListOf<PostHome>()
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_posts)

        // Gestion utilisateur
        val userManager = UserManager(this)
        user = userManager.getUser() ?: run {
            finish()
            return
        }

        // Bouton retour
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, ProfileOtherActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
            finish()
        }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSavedPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(savedPosts, user)
        recyclerView.adapter = postAdapter

        fetchSavedPosts()
    }

    private fun fetchSavedPosts() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<com.master.traveler.data.ApiResponse> =
                    RetrofitInstance.api.getSavedPosts(user.id)

                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()!!.posts

                    launch(Dispatchers.Main) {
                        savedPosts.clear()
                        savedPosts.addAll(posts)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@SavedPostsActivity,
                            "Erreur chargement posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@SavedPostsActivity,
                        "Erreur réseau: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

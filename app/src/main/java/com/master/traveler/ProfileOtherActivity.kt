package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfileOtherActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private var user: User? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_other)

        val user_id = intent.getStringExtra("USER_ID")

        val followButton: Button = findViewById(R.id.followButton)
        val reportButton: ImageView = findViewById(R.id.reportImage)

        userManager = UserManager(this)
        currentUser = userManager.getUser()

        if (user_id != null) {
            fetchUserData(user_id)
        }

        findViewById<ImageView>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val savedPostsButton: Button = findViewById(R.id.savedPostsButton)

        if (currentUser?.id == user_id) {
            savedPostsButton.visibility = View.VISIBLE
            savedPostsButton.setOnClickListener {
                val Intent = Intent(this, SavedPostsActivity::class.java)
                startActivity(Intent)
                finish()
                Toast.makeText(this, "Affichage des posts sauvegardés", Toast.LENGTH_SHORT).show()
            }

            // Print visibilité savedPostsButton
            println("savedPostsButton.visibility : ${savedPostsButton.visibility}")

            findViewById<ImageView>(R.id.editProfileButton).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.editProfileButton).setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java))
                finish()
            }

            followButton.visibility = View.GONE
            reportButton.visibility = View.GONE
        } else {
            reportButton.setOnClickListener {
                showReportMenu(it)
            }

            var isFollowing = currentUser?.following?.contains(user_id) == true

            updateFollowButton(followButton, isFollowing)

            followButton.setOnClickListener {
                isFollowing = !isFollowing
                toggleFollowUser(user_id, isFollowing, followButton)
            }
        }
    }

    private fun toggleFollowUser(userId: String?, isFollowing: Boolean, button: Button) {
        userId?.let {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response: Response<ApiResponse> = RetrofitInstance.api.followUser(currentUser!!.id, it, isFollowing)
                    if (response.isSuccessful) {
                        userManager.toggleFollowUser(it)
                        launch(Dispatchers.Main) {
                            updateFollowButton(button, isFollowing)
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            Toast.makeText(this@ProfileOtherActivity, "Erreur lors de la mise à jour du suivi", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@ProfileOtherActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFollowButton(button: Button, isFollowing: Boolean) {
        if (isFollowing) {
            button.text = getString(R.string.following_button)
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.button_light))
        } else {
            button.text = getString(R.string.follow_button)
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.background_light))
        }
    }

    private fun fetchUserData(userId: String) {
        // Utiliser RetrofitInstance pour obtenir l'API
        val apiService = RetrofitInstance.api

        // Récupérer les informations de l'utilisateur
        GlobalScope.launch(Dispatchers.IO) {
            try {
                println("User ID : $userId")
                val response = apiService.getUserInfos(userId)
                println("Response :")
                println(response)
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        // Mettre à jour l'UI avec les infos de l'utilisateur
                        this@ProfileOtherActivity.user = user  // Sauvegarde l'utilisateur pour l'utiliser partout
                        launch(Dispatchers.Main) {
                            updateUI(user)
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@ProfileOtherActivity, "Erreur lors de la récupération des données utilisateur", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@ProfileOtherActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Récupérer les posts de l'utilisateur
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getPostsFromUser(userId)
                if (response.isSuccessful) {
                    val code = response.body()?.code
                    if(code == 200) {
                        val posts = response.body()?.posts
                        // Afficher les posts dans un RecyclerView
                        launch(Dispatchers.Main) {
                            println("Posts :")
                            println(posts)
                            showUserPosts(posts)
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@ProfileOtherActivity, "Erreur lors de la récupération des posts", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@ProfileOtherActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUserPosts(posts: List<PostHome>?) {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        currentUser?.let {
            val postAdapter = PostAdapter(posts ?: emptyList(), it)
            recyclerView.adapter = postAdapter
        } ?: run {
            Toast.makeText(this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(user: User) {
        findViewById<TextView>(R.id.userUsername).text = user.username
        findViewById<TextView>(R.id.userBio).text = user.bio

        Glide.with(this)
            .load(user.profilePicture)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(findViewById(R.id.profilePicture))
    }

    private fun showReportMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_report, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.report_abuse -> {
                    Toast.makeText(this, getString(R.string.report_abuse), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.report_identity -> {
                    Toast.makeText(this, getString(R.string.report_identity), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.report_incorrect_posts -> {
                    Toast.makeText(this, getString(R.string.report_posts), Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}

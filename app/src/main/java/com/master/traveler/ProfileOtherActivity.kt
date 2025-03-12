package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProfileOtherActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private var user: User? = null  // Ajout du champ user pour le garder en référence
    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_other)

        val user_id = intent.getStringExtra("USER_ID")

        // Références aux Views
        val userLogin: TextView = findViewById(R.id.userUsername)
        val userBio: TextView = findViewById(R.id.userBio)
        val userPicture: ImageView = findViewById(R.id.profilePicture)

        userManager = UserManager(this)

        currentUser = userManager.getUser()

        // Chargement de l'image avec Glide
        if (user_id != null) {
            fetchUserData(user_id)
        }

        // Bouton pour retourner à HomeActivity
        findViewById<ImageView>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val reportButton: ImageView = findViewById(R.id.reportImage)

        // Gérer le clic sur le bouton "Signaler"
        reportButton.setOnClickListener {
            showReportMenu(it)
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


    private fun updateUI(user: User) {
        val userLogin: TextView = findViewById(R.id.userUsername)
        val userBio: TextView = findViewById(R.id.userBio)
        val userPicture: ImageView = findViewById(R.id.profilePicture)

        println("User: ")
        println(user)

        userLogin.text = user.username
        userBio.text = user.bio

        Glide.with(this)
            .load(user.profilePicture)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background)
            .circleCrop()
            .into(userPicture)
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


    private fun showReportMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_report, popupMenu.menu)

        // Gérer les clics sur les éléments du menu
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

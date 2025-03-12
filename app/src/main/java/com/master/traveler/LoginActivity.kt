package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.*
import com.master.traveler.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val url = "http://10.0.2.2:8000" // Adresse de ton API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton: Button = binding.loginButton

        loginButton.setOnClickListener {
            val login = binding.loginInput.text.toString()
            val pass = binding.passInput.text.toString()
            login(login, pass)
        }
    }

    // Fonction pour appeler l'API de connexion
    private fun login(login: String, pass: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$url/login/$login/password/$pass")
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = responseBody?.let { JSONObject(it) }
                    val code = jsonResponse?.optInt("code", 404)

                    if (code == 200) {
                        val userManager = UserManager(this@LoginActivity)
                        val userJson = jsonResponse.getJSONObject("user")

                        // Récupérer la liste "posts_liked" depuis le JSON
                        val postsLikedJsonArray = userJson.getJSONArray("posts_liked")

                        // Convertir le JSONArray en une liste de chaînes (List<String>)
                        val postsLikedList = mutableListOf<String>()
                        for (i in 0 until postsLikedJsonArray.length()) {
                            postsLikedList.add(postsLikedJsonArray.getString(i))
                        }

                        // Récupérer la liste "posts_saved" depuis le JSON
                        val postsSavedJsonArray = userJson.getJSONArray("posts_saved")

                        val postsSavedList = mutableListOf<String>()
                        for (i in 0 until postsSavedJsonArray.length()) {
                            postsSavedList.add(postsSavedJsonArray.getString(i))
                        }

                        val followersJsonArray = userJson.getJSONArray("followers")

                        val followersList = mutableListOf<String>()
                        for (i in 0 until followersJsonArray.length()) {
                            followersList.add(followersJsonArray.getString(i))
                        }

                        val followingJsonArray = userJson.getJSONArray("following")

                        val followingList = mutableListOf<String>()
                        for (i in 0 until followingJsonArray.length()) {
                            followingList.add(followingJsonArray.getString(i))
                        }

                        // Créer l'objet User avec toutes les données
                        val user = User(
                            id = userJson.getString("id"),
                            login = userJson.getString("login"),
                            username = userJson.getString("username"),
                            bio = userJson.getString("bio"),
                            profilePicture = userJson.getString("profilePicture"),
                            nbFollowers = userJson.getInt("nbFollowers"),
                            nbFollowing = userJson.getInt("nbFollowing"),
                            posts_liked = postsLikedList,
                            posts_saved = postsSavedList,
                            followers = followersList,
                            following = followingList,
                            travels = userJson.getInt("travels")
                        )

                        userManager.saveUser(user)

                        //addPost(user.id)

                        launch(Dispatchers.Main) {
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            binding.errorLogin.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Affiche l'exception complète dans Logcat
                launch(Dispatchers.Main) {
                    binding.errorLogin.text = "Erreur: ${e.message}"
                    binding.errorLogin.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun addPost(userId: String) {
        // Création de l'objet TravelInfos directement
        val postId = UUID.randomUUID().toString()

        val travelInfos = TravelInfos(
            arrival_date = "2025-01-01",
            departure_date = "2025-01-15",
            airplane = true,
            company_infos = CompanyInfos(
                name = "Air France",
                price = 640f, // Utilise un Float
                flight_link = "https://tinyurl.com/3skeefvf"
            ),
            activities = listOf(
                Activity(
                    name = "Machu Picchu",
                    price = 60f, // Utilise un Float
                    address = "Peru"
                )
            ),
            accommodations = listOf(
                Accommodation(
                    food = listOf(
                        Food(name = "Breakfast", price = 60f), // Utilise un Float
                        Food(name = "Lunch", price = 20f) // Utilise un Float
                    ),
                    hotel = listOf(Hotel(name = "Hotel", price = 180f)), // Utilise un Float
                    transport = listOf(Transport(name = "Taxi", price = 10f)) // Utilise un Float
                )
            ),
            pictures = listOf(Picture(url = "https://tinyurl.com/3skeefvf")),
            description = "C'était top, les paysages sont beaux, la mer est belle, la météo est cool, allez au pérou"
        )

        // Création de l'objet Post
        val post = Post(
            id = postId,
            user_id = "4dbe922b-0d19-46f7-872f-84441b8934c8",
            name = "Salty Springs",
            post_date = "2025-01-20",
            travel_infos = travelInfos,  // Utilise l'objet TravelInfos ici
            total_price = 80,
            presentation = Presentation(
                image = "https://i.redd.it/6mszgs0yi0c91.png",
                total_time = 14,
                card_color = "#A67D56",
                text_color = "#FFFFFF"
            ),
            nb_comments = 0,
            comments = emptyList(),
            likes = 2847,
            user_name = "marou"
        )

        // Appel réseau pour ajouter le post
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.addPost(post)
                if (response.isSuccessful && response.body()?.code == 200) {
                    println("Post ajouté avec succès")
                } else {
                    println("Erreur lors de l'ajout du post : ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        }
    }

}

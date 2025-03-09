package com.master.traveler

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProfileOtherActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_other)

        // Initialisation de UserManager
        userManager = UserManager(this)

        // Récupération de l'utilisateur sauvegardé
        val user = userManager.getUser()

        // Références aux Views
        val userLogin: TextView = findViewById(R.id.userUsername)
        val userBio: TextView = findViewById(R.id.userBio)
        val userPicture: ImageView = findViewById(R.id.profilePicture) // ImageView ici

        // Mise à jour des TextViews avec les informations de l'utilisateur
        if (user != null) {
            userLogin.text = "${user.login}"
            userBio.text = "${user.bio}"

            // Charger l'image avec Glide
            Glide.with(this)
                .load(user.profilePicture)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .circleCrop()
                .into(userPicture)
        } else {
            userLogin.text = "Login inconnu"
            userBio.text = "Bio non définie"

            // Image par défaut si pas d'utilisateur
            userPicture.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
}

package com.master.traveler

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.master.traveler.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val url = "http://10.0.2.2:8000" // Adresse de ton API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonHello: Button = findViewById(R.id.button_hello)
        val loginInput: EditText = findViewById(R.id.login_input)
        val passInput: EditText = findViewById(R.id.pass_input)
        val loginButton: Button = findViewById(R.id.login_button)

        // Quand on clique sur le bouton, on lance la requête à l'API
        buttonHello.setOnClickListener {
            callApi()
        }

        loginButton.setOnClickListener{
            val login = loginInput.text.toString()
            val pass = passInput.text.toString()
            login(login, pass)
        }
    }

    // Fonction pour appeler l'API de connexion
    private fun login(login: String, pass: String){
        GlobalScope.launch(Dispatchers.IO){
            try{
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$url/login/$login/password/$pass")
                    .build()

                val response: Response = client.newCall(request).execute()

                if(response.isSuccessful){
                    val responseBody = response.body?.string()
                    launch(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Réponse: $responseBody", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    launch(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "Erreur API: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // Fonction pour appeler l'API
    private fun callApi() {
        // Lancer la requête HTTP dans un thread de fond
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$url/")  // URL de ton API
                    .build()

                val response: Response = client.newCall(request).execute()

                // Vérifie si la réponse est réussie
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Utiliser le thread principal pour afficher le résultat
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Réponse: $responseBody", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Affiche une erreur si la requête a échoué
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Erreur API: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Gère les erreurs de réseau
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

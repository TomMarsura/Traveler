package com.master.traveler

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.master.traveler.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

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
        val responseText: TextView = findViewById(R.id.responseText)

        // Quand on clique sur le bouton, on lance la requête à l'API
        buttonHello.setOnClickListener {
            callApi(responseText)
        }

        loginButton.setOnClickListener {
            val login = loginInput.text.toString()
            val pass = passInput.text.toString()
            login(login, pass, responseText)
        }
    }

    // Fonction pour appeler l'API de connexion
    private fun login(login: String, pass: String, responseText: TextView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$url/login/$login/password/$pass")
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val message = responseBody?.let { JSONObject(it).optString("message", "Réponse vide") }
                    launch(Dispatchers.Main) {
                        responseText.text = message
                    }
                } else {
                    launch(Dispatchers.Main) {
                        responseText.text = "Erreur API: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    responseText.text = "Erreur: ${e.message}"
                }
            }
        }
    }

    // Fonction pour appeler l'API
    private fun callApi(responseText: TextView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$url/")
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val message = responseBody?.let { JSONObject(it).optString("message", "Réponse vide") }
                    launch(Dispatchers.Main) {
                        responseText.text = message
                    }
                } else {
                    launch(Dispatchers.Main) {
                        responseText.text = "Erreur API: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    responseText.text = "Erreur: ${e.message}"
                }
            }
        }
    }
}

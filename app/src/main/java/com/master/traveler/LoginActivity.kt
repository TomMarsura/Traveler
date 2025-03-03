package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.master.traveler.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

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

                    launch(Dispatchers.Main) {
                        if (code == 200) {
                            // Cacher le message d'erreur si le login est correct
                            binding.errorLogin.visibility = View.INVISIBLE

                            // Rediriger vers HomeActivity
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish() // Ferme l'activité actuelle
                        } else {
                            // Afficher le message d'erreur si le login est incorrect
                            binding.errorLogin.visibility = View.VISIBLE
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        binding.errorLogin.text = "Erreur API: ${response.code}"
                        binding.errorLogin.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.errorLogin.text = "Erreur: ${e.message}"
                    binding.errorLogin.visibility = View.VISIBLE
                }
            }
        }
    }
}

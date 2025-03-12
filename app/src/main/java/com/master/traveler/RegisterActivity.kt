package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.master.traveler.databinding.RegisterActivityBinding
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passInput.text.toString().trim()
            val login = binding.loginInput.text.toString().trim()


            if (username.isEmpty() || login.isEmpty() || password.isEmpty()) {
                binding.errorRegister.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.errorRegister.visibility = View.VISIBLE
                return@setOnClickListener
            }

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response: Response<ApiResponse> =
                        RetrofitInstance.api.registerUser(login, password, username)

                    if (response.isSuccessful) {
                        val code = response.body()?.code
                        if (code == 200) {
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else if (code == 406) {
                            binding.errorRegister.text = "Login déjà utilisé"
                            binding.errorRegister.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) {
                        binding.errorRegister.text = "Erreur: ${e.message}"
                        binding.errorRegister.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}



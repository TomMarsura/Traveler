package com.master.traveler

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private var currentUser: User? = null
    private lateinit var profileImageView: ImageView
    private lateinit var bioEditText: EditText
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        userManager = UserManager(this)
        currentUser = userManager.getUser()

        profileImageView = findViewById(R.id.profilePicture)
        bioEditText = findViewById(R.id.bioInput)

        loadUserData()

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, ProfileOtherActivity::class.java)
            intent.putExtra("USER_ID", currentUser?.id)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.changeProfileImageButton).setOnClickListener {
            showImageSelectionDialog()
        }

        findViewById<Button>(R.id.changeLoginButton).setOnClickListener { showCustomDialog(R.layout.new_login_dialog, "Changer Login") }
        findViewById<Button>(R.id.changeUsernameButton).setOnClickListener { showCustomDialog(R.layout.new_username_dialog, "Changer Username") }
        findViewById<Button>(R.id.changePasswordButton).setOnClickListener { showCustomDialog(R.layout.new_password_dialog, "Changer Mot de Passe") }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        currentUser?.let { user ->
            Glide.with(this)
                .load(user.profilePicture)
                .circleCrop()
                .into(profileImageView)
            bioEditText.setText(user.bio)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun showCustomDialog(layoutId: Int, title: String) {
        val dialogView = LayoutInflater.from(this).inflate(layoutId, null)

        val inputField = dialogView.findViewById<EditText>(R.id.inputField)
        val inputNewPassword = dialogView.findViewById<EditText>(R.id.inputNewPassword)
        val inputConfirmPassword = dialogView.findViewById<EditText>(R.id.inputConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Confirmer") { _, _ ->
                when (layoutId) {
                    R.layout.new_login_dialog, R.layout.new_username_dialog -> {
                        val inputValue = inputField?.text.toString()
                        if (inputValue.isNotEmpty()) {
                            when (layoutId) {
                                R.layout.new_login_dialog -> updateLogin(inputValue)
                                R.layout.new_username_dialog -> updateUsername(inputValue)
                            }
                        } else {
                            Toast.makeText(this, "Le champ ne peut pas être vide", Toast.LENGTH_SHORT).show()
                        }
                    }
                    R.layout.new_password_dialog -> {
                        val newPassword = inputNewPassword?.text.toString()
                        val confirmPassword = inputConfirmPassword?.text.toString()
                        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                            Toast.makeText(this, "Tous les champs doivent être remplis", Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmPassword) {
                            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                        } else {
                            updatePassword(newPassword)
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun updateLogin(newLogin: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.updateLogin(currentUser!!.id, newLogin)
                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userManager.setLogin(newLogin)
                        Toast.makeText(this@EditProfileActivity, "Login mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erreur lors de la mise à jour du login", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUsername(newUsername: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.updateUsername(currentUser!!.id, newUsername)
                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        userManager.setUsername(newUsername)
                        Toast.makeText(this@EditProfileActivity, "Username mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erreur lors de la mise à jour du username", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.updatePassword(currentUser!!.id, newPassword)
                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erreur lors de la mise à jour du mot de passe", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImageSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile_images, null)
        val container = dialogView.findViewById<GridLayout>(R.id.imageContainer)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choisissez une image de profil")
            .setView(dialogView)
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()  // Affiche directement le loader pendant chargement

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getProfileImages()

                if (response.isSuccessful) {
                    val urls = response.body()?.images  // Doit être une `List<String>` dans ApiResponse

                    urls?.forEach { url ->
                        launch(Dispatchers.Main) {
                            val imageView = ImageView(this@EditProfileActivity)
                            imageView.layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                                setMargins(16, 16, 16, 16)
                            }
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                            Glide.with(this@EditProfileActivity)
                                .load(url)
                                .circleCrop()
                                .into(imageView)

                            imageView.setOnClickListener {
                                updateProfileImage(url)
                                dialog.dismiss()
                            }

                            container.addView(imageView)
                        }
                    }
                } else {
                    launch(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileActivity, "Erreur API", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfileImage(url: String) {
        val imageUrlPayload = mapOf("url" to url)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.updateProfileImage(currentUser!!.id, imageUrlPayload)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Mise à jour UI
                        Glide.with(this@EditProfileActivity)
                            .load(url)
                            .circleCrop()
                            .into(profileImageView)

                        // Mise à jour User local
                        val updatedUser = currentUser!!.copy(profilePicture = url)
                        userManager.saveUser(updatedUser)
                        currentUser = updatedUser

                        Toast.makeText(this@EditProfileActivity, "Image mise à jour", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erreur API: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveChanges() {
        val newBio = bioEditText.text.toString()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.updateBio(currentUser!!.id, newBio)

                launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 🔥 Met à jour la bio localement
                        userManager.setBio(newBio)

                        Toast.makeText(this@EditProfileActivity, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@EditProfileActivity, ProfileOtherActivity::class.java)
                        intent.putExtra("USER_ID", currentUser?.id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erreur réseau: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

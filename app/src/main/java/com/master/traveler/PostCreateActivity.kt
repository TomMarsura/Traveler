package com.master.traveler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.Accommodation
import com.master.traveler.data.Activity
import com.master.traveler.data.CompanyInfos
import com.master.traveler.data.Food
import com.master.traveler.data.Hotel
import com.master.traveler.data.Picture
import com.master.traveler.data.Post
import com.master.traveler.data.Presentation
import com.master.traveler.data.Transport
import com.master.traveler.data.TravelInfos
import com.master.traveler.data.User
import com.master.traveler.databinding.PostCreationActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale
import java.util.UUID
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit


class PostCreateActivity : AppCompatActivity() {
    private lateinit var binding: PostCreationActivityBinding
    private lateinit var user: User
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                lifecycleScope.launch {
                    val uploadedUrl = uploadImageToFreeImage(this@PostCreateActivity, it)
                    if (uploadedUrl != null) {
                        val imageView = ImageView(this@PostCreateActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                                marginEnd = 8
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            tag = uploadedUrl // <- pour récupérer plus tard
                        }

                        Glide.with(this@PostCreateActivity)
                            .load(uploadedUrl)
                            .into(imageView)

                        binding.idPhotoGallery.addView(imageView)
                    } else {
                        Toast.makeText(this@PostCreateActivity, "Erreur upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private var selectedStartDate: Long = 0L
    private var selectedEndDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostCreationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var start = ""
        var end = ""

// Récupérer la date par défaut à l'ouverture
        start = formatDate(binding.idCalendarStart.date)
        end = formatDate(binding.idCalendarEnd.date)

// Mettre à jour automatiquement quand l'utilisateur change une date
        binding.idCalendarStart.setOnDateChangeListener { _, year, month, dayOfMonth ->
            start = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        binding.idCalendarEnd.setOnDateChangeListener { _, year, month, dayOfMonth ->
            end = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        binding.idCheckboxPlane.isChecked = false
        binding.idSpinnerCompany.isEnabled = false
        binding.idPrice.isEnabled = false
        binding.idLinkFlight.isEnabled = false

        binding.idPrice.isFocusable = false
        binding.idPrice.isFocusableInTouchMode = false
        binding.idLinkFlight.isFocusable = false
        binding.idLinkFlight.isFocusableInTouchMode = false

        binding.idPrice.alpha = 0.5f
        binding.idLinkFlight.alpha = 0.5f
        binding.idSpinnerCompany.alpha = 0.5f



        val userManager = UserManager(this)
        user = userManager.getUser() ?: run {
            finish()
            return
        }
        findViewById<ImageView>(R.id.id_back_button).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
            finish()
        }

        binding.idBtnAddPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.idPrice.doAfterTextChanged{
            updateTotal()
        }
        binding.idBtnContinue.setOnClickListener {
            val description = binding.idDescription.text.toString()
            val mutableActivities = mutableListOf<Activity>()
            val foodList = mutableListOf<Food>()
            val hotelList = mutableListOf<Hotel>()
            val transportList = mutableListOf<Transport>()
            val imageUrls = mutableListOf<String>()
            val destination = binding.idDestination.text.toString().trim()
            val isPlaneChecked = binding.idCheckboxPlane.isChecked
            val priceStr = binding.idPrice.text.toString().trim()
            val link = binding.idLinkFlight.text.toString().trim()
            val selectedCompany = binding.idSpinnerCompany.selectedItem.toString()
            val companyInfos: CompanyInfos

            if (destination.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une destination", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (isPlaneChecked) {
                val priceValue = priceStr.toFloatOrNull()
                if (selectedCompany.isEmpty() || priceStr.isEmpty() || link.isEmpty() || priceValue == null || priceValue <= 0f) {
                    Toast.makeText(this, "Veuillez compléter correctement les informations de vol", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                companyInfos = CompanyInfos(
                    name = selectedCompany,
                    price = priceValue,
                    flight_link = link
                )
            } else {
                companyInfos = CompanyInfos(
                    name = "",
                    price = 0f,
                    flight_link = ""
                )
            }

            // Validation 3 : au moins une image uploadée
            if (binding.idPhotoGallery.childCount == 0) {
                Toast.makeText(this, "Veuillez ajouter au moins une photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toFloatOrNull() ?: 0f

            for (i in 0 until binding.idPlacesList.childCount) {
                val row = binding.idPlacesList.getChildAt(i) as? LinearLayout ?: continue
                if (row.childCount < 2) continue

                val nameEditText = row.getChildAt(0) as? EditText
                val priceEditText = row.getChildAt(1) as? EditText

                val name = nameEditText?.text?.toString()?.trim().orEmpty()
                val priceTemp = priceEditText?.text?.toString()?.toFloatOrNull() ?: 0f

                if (name.isNotEmpty()) {
                    mutableActivities.add(Activity(name = name, price = priceTemp))
                }
            }


            for (i in 0 until binding.idAccommodationList.childCount) {
                val row = binding.idAccommodationList.getChildAt(i) as? LinearLayout ?: continue
                val nameEditText = row.getChildAt(0) as? EditText ?: continue
                val priceEditText = row.getChildAt(1) as? EditText ?: continue

                val type = nameEditText.tag.toString()
                val name = nameEditText.text.toString().trim()
                val price = priceEditText.text.toString().toFloatOrNull() ?: 0f

                if (name.isNotEmpty()) {
                    when (type) {
                        "Food" -> foodList.add(Food(name, price))
                        "Hotel" -> hotelList.add(Hotel(name, price))
                        "Transport" -> transportList.add(Transport(name, price))
                    }
                }
            }
            for (i in 0 until binding.idPhotoGallery.childCount) {
                val imageView = binding.idPhotoGallery.getChildAt(i) as? ImageView ?: continue
                val url = imageView.tag?.toString()
                if (!url.isNullOrEmpty()) {
                    imageUrls.add(url)
                }
            }

            val pic = imageUrls.map { Picture(url = it) }


            val postId = UUID.randomUUID().toString()
            val list = mutableActivities.toList()
            val travelInfos = TravelInfos(
                arrival_date = end,
                departure_date = start,
                airplane = binding.idCheckboxPlane.isChecked,
                company_infos = companyInfos,
                activities = list,
                accommodations = listOf(
                    Accommodation(
                        food = foodList,
                        hotel = hotelList,
                        transport = transportList
                    )
                ),
                pictures = pic,
                description = description
            )

            // Création de l'objet Post
            val startDate = LocalDate.parse(start)
            val endDate = LocalDate.parse(end)
            val duration = ChronoUnit.DAYS.between(startDate, endDate).toInt()

            if (duration < 0) {
                Toast.makeText(this, "La date de fin ne peut pas être avant la date de début", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = calculateTotal().toInt()

            val post = Post(
                id = postId,
                user_id = user.id,
                name = destination,
                post_date = LocalDate.now().toString(),
                travel_infos = travelInfos,
                total_price = totalPrice,  // maintenant c'est vraiment correct !
                presentation = Presentation(
                    image = pic[0].url,
                    total_time = duration,
                    card_color = "#A67D56",
                    text_color = "#FFFFFF"
                ),
                nb_comments = 0,
                comments = emptyList(),
                likes = 0,
                user_name = user.username
            )

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitInstance.api.addPost(post)
                    if (response.isSuccessful && response.body()?.code == 200) {
                        println("Post ajouté avec succès")
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@PostCreateActivity, HomeActivity::class.java)
                            intent.putExtra("USER_ID", user.id)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        println("Erreur lors de l'ajout du post : ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                }
            }

        }

        binding.idBtnAddPlace.setOnClickListener {
            val context = this

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            }

            val placeEditText = EditText(context).apply {
                hint = context.getString(R.string.activity_name)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            }

            val priceEditText = EditText(context).apply {
                hint = context.getString(R.string.activity_price)
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 8
                }

                doAfterTextChanged {
                    updateTotal()
                }
            }


            row.addView(placeEditText)
            row.addView(priceEditText)
            binding.idPlacesList.addView(row)

            updateTotal()


        }
        binding.idBtnAddAccommodation.setOnClickListener {
            val context = this
            val selectedType = binding.idSpinnerAccommodationType.selectedItem.toString()

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            }

            val nameEditText = EditText(context).apply {
                hint = getString(R.string.accommodation_name, selectedType)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                tag = selectedType
            }

            val priceEditText = EditText(context).apply {
                hint = getString(R.string.activity_price)
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 8
                }

                doAfterTextChanged { updateTotal() }
            }

            row.addView(nameEditText)
            row.addView(priceEditText)
            binding.idAccommodationList.addView(row)

            updateTotal()
        }

        binding.idCheckboxPlane.setOnCheckedChangeListener { _, isChecked ->
            binding.idSpinnerCompany.isEnabled = isChecked
            binding.idPrice.isEnabled = isChecked
            binding.idLinkFlight.isEnabled = isChecked

            binding.idPrice.isFocusable = isChecked
            binding.idPrice.isFocusableInTouchMode = isChecked

            binding.idLinkFlight.isFocusable = isChecked
            binding.idLinkFlight.isFocusableInTouchMode = isChecked

            binding.idPrice.alpha = if (isChecked) 1f else 0.5f
            binding.idLinkFlight.alpha = if (isChecked) 1f else 0.5f
            binding.idSpinnerCompany.alpha = if (isChecked) 1f else 0.5f
        }

    }

    private fun calculateTotal(): Float {
        var totalActivities = 0f
        var totalAccommodations = 0f

        for (i in 0 until binding.idPlacesList.childCount) {
            val row = binding.idPlacesList.getChildAt(i) as? LinearLayout ?: continue
            if (row.childCount < 2) continue

            val priceEditText = row.getChildAt(1) as? EditText
            val price = priceEditText?.text?.toString()?.toFloatOrNull() ?: 0f
            totalActivities += price
        }

        for (i in 0 until binding.idAccommodationList.childCount) {
            val row = binding.idAccommodationList.getChildAt(i) as? LinearLayout ?: continue
            if (row.childCount < 2) continue

            val priceEditText = row.getChildAt(1) as? EditText
            val price = priceEditText?.text?.toString()?.toFloatOrNull() ?: 0f
            totalAccommodations += price
        }

        val flightPrice = binding.idPrice.text.toString().toFloatOrNull() ?: 0f

        return flightPrice + totalActivities + totalAccommodations
    }

    private fun updateTotal() {
        var totalActivities = 0f
        var totalAccommodations = 0f

        for (i in 0 until binding.idPlacesList.childCount) {
            val row = binding.idPlacesList.getChildAt(i) as? LinearLayout ?: continue
            if (row.childCount < 2) continue

            val priceEditText = row.getChildAt(1) as? EditText
            val price = priceEditText?.text?.toString()?.toFloatOrNull() ?: 0f
            totalActivities += price
        }

        for (i in 0 until binding.idAccommodationList.childCount) {
            val row = binding.idAccommodationList.getChildAt(i) as? LinearLayout ?: continue
            if (row.childCount < 2) continue

            val priceEditText = row.getChildAt(1) as? EditText
            val price = priceEditText?.text?.toString()?.toFloatOrNull() ?: 0f
            totalAccommodations += price
        }

        val flightPrice = binding.idPrice.text.toString().toFloatOrNull() ?: 0f

        val total = flightPrice + totalActivities + totalAccommodations
        val formattedTotal = String.format(Locale.getDefault(), "Total : %.2f €", total)
        binding.idTotal.text = formattedTotal
    }


    private suspend fun uploadImageToFreeImage(context: Context, imageUri: Uri): String? {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri) ?: return null
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val file = File(context.cacheDir, fileName)

        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("source", file.name, requestFile)

        val apiKey = "6d207e02198a847aa98d0a2a901485a5".toRequestBody("text/plain".toMediaTypeOrNull())
        val format = "json".toRequestBody("text/plain".toMediaTypeOrNull())

        val response = RetrofitInstance.freeImageApi.uploadImage(apiKey, multipartBody, format)

        return if (response.isSuccessful) {
            response.body()?.image?.url
        } else {
            Log.e("UPLOAD", "Erreur: ${response.errorBody()?.string()}")
            null
        }
    }
    private fun formatDate(dateInMillis: Long): String {
        val localDate = Instant.ofEpochMilli(dateInMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.toString() // Renvoie directement "yyyy-MM-dd"
    }

}

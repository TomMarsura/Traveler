package com.master.traveler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.Picture
import kotlinx.coroutines.*
import retrofit2.Response

class PostActivity : AppCompatActivity() {

    private lateinit var viewPagerImages: ViewPager2
    private lateinit var postName: TextView
    private lateinit var postDate: TextView
    private lateinit var datesVoyage: TextView
    private lateinit var flightInfos: TextView
    private lateinit var activitiesLayout: LinearLayout
    private lateinit var accommodationsLayout: LinearLayout
    private lateinit var description: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Init views
        viewPagerImages = findViewById(R.id.viewPagerImages)
        postName = findViewById(R.id.postName)
        postDate = findViewById(R.id.postDate)
        datesVoyage = findViewById(R.id.datesVoyage)
        flightInfos = findViewById(R.id.flightInfos)
        activitiesLayout = findViewById(R.id.activitiesLayout)
        accommodationsLayout = findViewById(R.id.accommodationsLayout)
        description = findViewById(R.id.description)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        // Récupérer l'ID du post
        val postId = intent.getStringExtra("postId") ?: return

        // Fetch les données du post
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.getPost(postId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    val post = response.body()?.post
                    val images = post?.travel_infos?.pictures?.map { it.url } ?: emptyList()

                    launch(Dispatchers.Main) {
                        post?.let { p ->
                            postName.text = p.name
                            postDate.text = getString(R.string.post_date, p.post_date)
                            datesVoyage.text = getString(R.string.travel_dates, p.travel_infos.departure_date, p.travel_infos.arrival_date)

                            val company = p.travel_infos.company_infos
                            flightInfos.text = getString(R.string.flight_infos, company.name, company.price)

                            description.text = p.travel_infos.description

                            // Ajouter les activités
                            activitiesLayout.removeAllViews()
                            for (activity in p.travel_infos.activities) {
                                val activityView = TextView(this@PostActivity)
                                activityView.text = "${activity.name} - ${activity.price} €"
                                activityView.textSize = 14f
                                activitiesLayout.addView(activityView)
                            }

                            // Ajouter les hébergements
                            accommodationsLayout.removeAllViews()
                            val acc = p.travel_infos.accommodations.firstOrNull()
                            acc?.let {
                                if (it.hotel.isNotEmpty()) {
                                    accommodationsLayout.addView(sectionTitle(getString(R.string.hostels)))
                                    it.hotel.forEach { hotel ->
                                        accommodationsLayout.addView(infoText("${hotel.name} - ${hotel.price} €"))
                                    }
                                }

                                if (it.food.isNotEmpty()) {
                                    accommodationsLayout.addView(sectionTitle(getString(R.string.food)))
                                    it.food.forEach { food ->
                                        accommodationsLayout.addView(infoText("${food.name} - ${food.price} €"))
                                    }
                                }

                                if (it.transport.isNotEmpty()) {
                                    accommodationsLayout.addView(sectionTitle(getString(R.string.transport)))
                                    it.transport.forEach { transport ->
                                        accommodationsLayout.addView(infoText("${transport.name} - ${transport.price} €"))
                                    }
                                }
                            }

                            // Carousel d'images
                            viewPagerImages.adapter = CarouselAdapter(images)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement du post: ${e.message}")
            }
        }
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setPadding(0, 8, 0, 4)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun infoText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
        }
    }

    // Adapter pour le carrousel d'images
    inner class CarouselAdapter(private val images: List<String>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<CarouselAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(itemView: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageSlide)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = layoutInflater.inflate(R.layout.item_image_carousel, parent, false)
            return ImageViewHolder(view)
        }


        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imageUrl = images[position]

            Glide.with(this@PostActivity)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .fitCenter()
                .into(holder.imageView)

            holder.imageView.setOnClickListener {
                val intent = Intent(this@PostActivity, ImageFullscreenActivity::class.java)
                intent.putExtra("imageUrl", imageUrl)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = images.size
    }
}
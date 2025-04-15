package com.master.traveler

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageFullscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_fullscreen)

        val imageUrl = intent.getStringExtra("imageUrl") ?: return
        val imageView = findViewById<ImageView>(R.id.fullscreenImage)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(imageView)

        // Fermer au clic
        imageView.setOnClickListener {
            finish()
        }
    }
}
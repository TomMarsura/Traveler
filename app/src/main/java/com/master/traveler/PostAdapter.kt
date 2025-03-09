package com.master.traveler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.PostHome
import com.master.traveler.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response

class PostAdapter(
    private val posts: List<PostHome>,
    private val user: User, // Ajout du user pour vérifier les posts likés
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Assigner les données du post à la vue
        holder.postName.text = post.name
        holder.postPrice.text = "€ ${post.total_price}"
        holder.postTime.text = "${post.presentation.total_time} jours"

        // Charger l'image avec Glide
        Glide.with(holder.itemView.context)
            .load(post.presentation.image)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background)
            .into(holder.postImage)

        // Afficher les likes et les commentaires
        holder.postLikeCount.text = post.likes.toString()
        holder.postCommentCount.text = post.nb_comments.toString()

        // Vérifier si ce post est liké par l'utilisateur
        var isLiked = user.posts_liked.contains(post.id)

        // Modifier l'état du bouton like (changer en rouge si liké)
        if (isLiked) {
            holder.postLikeButton.setImageResource(R.drawable.ic_heart_filled)
        } else {
            holder.postLikeButton.setImageResource(R.drawable.ic_heart_outline)
        }

        // Gestion des clics sur le bouton "Like"
        holder.postLikeButton.setOnClickListener {
            // Inverser l'état du like et mettre à jour la liste des posts likés
            isLiked = !isLiked
            // Appeler la fonction onLikeClicked pour mettre à jour l'interface utilisateur et les données de l'utilisateur
            onLikeClicked(holder, post.id, isLiked)

            // Mettre à jour l'UI en fonction de l'état du bouton
        }

        // Gestion des clics sur le bouton "Commentaire"
        holder.postCommentButton.setOnClickListener {
            // Ouvrir l'écran des commentaires ou effectuer une autre action
        }

        // Gestion des clics sur le bouton "Enregistrer" (Signet)
        holder.postBookmarkButton.setOnClickListener {
            // Ajouter la logique pour enregistrer un post
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder pour chaque carte de post
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val postName: TextView = itemView.findViewById(R.id.postName)
        val postPrice: TextView = itemView.findViewById(R.id.postPrice)
        val postTime: TextView = itemView.findViewById(R.id.postTime)

        // Nouvelles vues pour les likes, les commentaires et l'enregistrement
        val postLikeButton: ImageButton = itemView.findViewById(R.id.postLikeButton)
        val postLikeCount: TextView = itemView.findViewById(R.id.postLikeCount)
        val postCommentButton: ImageButton = itemView.findViewById(R.id.postCommentButton)
        val postCommentCount: TextView = itemView.findViewById(R.id.postCommentCount)
        val postBookmarkButton: ImageButton = itemView.findViewById(R.id.postBookmarkButton)
    }

    fun onLikeClicked(holder: PostViewHolder, postId: String, isLiked: Boolean) {
        // Mettre à jour l'interface utilisateur
        // Mettre à jour les données de l'utilisateur

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.likePost(postId, user.id, isLiked)

                if (response.isSuccessful) {
                    if (isLiked) {
                        holder.postLikeButton.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        holder.postLikeButton.setImageResource(R.drawable.ic_heart_outline)
                    }

                    // Mettre à jour le nombre de likes
                    val apiResponse = response.body()

                    apiResponse?.let {
                        holder.postLikeCount.text = it.nb_likes.toString()
                    }

                } else {
                    println("Error liking post")
                }
            } catch (e: Exception) {
                println("Error liking post: ${e.message}")
            }
        }
    }
}

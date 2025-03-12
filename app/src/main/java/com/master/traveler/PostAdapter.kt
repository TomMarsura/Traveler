package com.master.traveler

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.master.traveler.config.RetrofitInstance
import com.master.traveler.data.ApiResponse
import com.master.traveler.data.Comment
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

        println("Entrée POSTS")
        println("Post ID: ${post.id}")

        // Assigner les données du post à la vue
        holder.postName.text = post.name
        holder.postPrice.text = "€ ${post.total_price}"
        holder.postTime.text = "${post.presentation.total_time} jours"
        holder.userName.text = post.user_name

        val userProfilePicture = getProfilePicture(post.user_id, holder.userProfileImage)

        // Charger l'image avec Glide
        Glide.with(holder.itemView.context)
            .load(post.presentation.image)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background)
            .into(holder.postImage)

        // Afficher les likes et les commentaires
        holder.postLikeCount.text = post.likes.toString()
        holder.postCommentCount.text = post.nb_comments.toString()

        holder.userInfoLayout.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProfileOtherActivity::class.java)
            intent.putExtra("USER_ID", post.user_id)
            holder.itemView.context.startActivity(intent)
        }

        // Vérifier si ce post est liké par l'utilisateur
        var isLiked = user.posts_liked.contains(post.id)
        var isSaved = user.posts_saved.contains(post.id)

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
            onLikeClicked(holder, post.id, isLiked, UserManager(holder.itemView.context))

            // Mettre à jour l'UI en fonction de l'état du bouton
        }

        // Gestion des clics sur le bouton "Commentaire"
        holder.postCommentButton.setOnClickListener {
            showCommentsBottomSheet(holder, holder.itemView, post.id)
        }

        // Gestion des clics sur le bouton "Enregistrer" (Signet)
        holder.postBookmarkButton.setOnClickListener {
            isSaved = !isSaved
            onSaveClicked(holder, post.id, isSaved)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder pour chaque carte de post
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val userProfileImage: ImageView = itemView.findViewById(R.id.userProfileImage)
        val postName: TextView = itemView.findViewById(R.id.postName)
        val postPrice: TextView = itemView.findViewById(R.id.postPrice)
        val postTime: TextView = itemView.findViewById(R.id.postTime)
        val userName: TextView = itemView.findViewById(R.id.userName)

        // Nouvelles vues pour les likes, les commentaires et l'enregistrement
        val postLikeButton: ImageButton = itemView.findViewById(R.id.postLikeButton)
        val postLikeCount: TextView = itemView.findViewById(R.id.postLikeCount)
        val postCommentButton: ImageButton = itemView.findViewById(R.id.postCommentButton)
        val postCommentCount: TextView = itemView.findViewById(R.id.postCommentCount)
        val postBookmarkButton: ImageButton = itemView.findViewById(R.id.postBookmarkButton)

        // Nouveau LinearLayout
        val userInfoLayout: LinearLayout = itemView.findViewById(R.id.userInfo)
    }

    private fun getProfilePicture(userId: String, imageView: ImageView) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.getProfilePicture(userId)
                if (response.isSuccessful && response.body()?.code == 200) {
                    val profilePictureUrl = response.body()?.profilePicture
                    println("URL de la photo de profil: $profilePictureUrl")
                    launch(Dispatchers.Main) {
                        Glide.with(imageView.context)
                            .load(profilePictureUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(imageView)
                    }
                } else {
                    // Image par défaut en cas d'erreur
                    launch(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.ic_launcher_background)
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement de la photo de profil: ${e.message}")
                launch(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_launcher_background)
                }
            }
        }
    }

    fun onSaveClicked(holder: PostViewHolder, postId: String, isSaved: Boolean) {
        GlobalScope.launch(Dispatchers.IO){
            try {
                val response : Response<ApiResponse> = RetrofitInstance.api.savePost(postId, user.id, isSaved)

                if(response.isSuccessful){
                    if(isSaved) {
                        holder.postBookmarkButton.setImageResource(R.drawable.ic_bookmark_border_filled)
                    } else {
                        holder.postBookmarkButton.setImageResource(R.drawable.ic_bookmark_border)
                    }
                } else {
                    println("Error saving post")
                }
            } catch (e: Exception) {
                println("Error saving post: ${e.message}")
            }
        }
    }

    fun onLikeClicked(holder: PostViewHolder, postId: String, isLiked: Boolean, userManager: UserManager) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.likePost(postId, user.id, isLiked)

                if (response.isSuccessful) {
                    if (isLiked) {
                        holder.postLikeButton.setImageResource(R.drawable.ic_heart_filled)
                        userManager.toggleLikePost(postId)
                    } else {
                        holder.postLikeButton.setImageResource(R.drawable.ic_heart_outline)
                        userManager.toggleLikePost(postId)
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

    private fun showCommentsBottomSheet(holder: PostViewHolder, view: View, postId: String) {
        val context = view.context
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comments, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(bottomSheetView)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        val commentEditText = bottomSheetView.findViewById<TextView>(R.id.commentEditText)
        val sendButton = bottomSheetView.findViewById<ImageButton>(R.id.sendCommentButton)

        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = CommentAdapter(emptyList())

        // Charger les commentaires depuis l'API
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<ApiResponse> = RetrofitInstance.api.getComments(postId)

                if (response.isSuccessful && response.body() != null) {
                    val comments = response.body()?.comments ?: emptyList()

                    // Mettre à jour l'adaptateur sur le thread principal
                    launch(Dispatchers.Main) {
                        (recyclerView.adapter as CommentAdapter).updateComments(comments)
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement des commentaires: ${e.message}")
            }
        }

        // Gestion de l'envoi d'un nouveau commentaire
        sendButton.setOnClickListener {
            val commentText = commentEditText.text.toString()
            if (commentText.isNotEmpty()) {
                val comment = Comment(user.id, user.login, commentText)

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val response = RetrofitInstance.api.addComment(postId, comment)
                        if (response.isSuccessful) {
                            // Rafraîchir les commentaires après ajout
                            launch(Dispatchers.Main) {
                                commentEditText.text = ""
                                bottomSheetDialog.dismiss()
                                showCommentsBottomSheet(holder, view, postId)
                            }
                            // Changer le nombre de commentaires
                            val apiResponse = response.body()

                            apiResponse?.let {
                                holder.postCommentCount.text = it.nb_comments.toString()
                            }
                        } else {
                            println("Erreur API: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        println("Erreur lors de l'ajout du commentaire: ${e.message}")
                    }
                }

            }
        }

        bottomSheetDialog.show()
    }

}

package com.master.traveler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.master.traveler.data.Comment

class CommentAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged() // Notifier que les données ont changé
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentUser.text = comment.login
        holder.commentText.text = comment.comment
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentUser: TextView = itemView.findViewById(R.id.commentUser)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
    }


}

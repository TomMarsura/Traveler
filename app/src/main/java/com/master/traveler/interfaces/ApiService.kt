package com.master.traveler.interfaces

import com.master.traveler.data.ApiResponse
import com.master.traveler.data.Comment
import com.master.traveler.data.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("posts/add")
    suspend fun addPost(@Body post: Post): Response<ApiResponse>

    @GET("posts")
    suspend fun getPosts(): Response<ApiResponse>

    @POST("posts/like/{postId}/{userId}/{isLiked}")
    suspend fun likePost(
        @Path("postId") postId: String,
        @Path("userId") userId: String,
        @Path("isLiked") isLiked: Boolean
    ): Response<ApiResponse>

    @POST("posts/save/{postId}/{userId}/{isSaved}")
    suspend fun savePost(
        @Path("postId") postId: String,
        @Path("userId") userId: String,
        @Path("isSaved") isSaved: Boolean
    ): Response<ApiResponse>

    @GET("posts/get_comments/{postId}")
    suspend fun getComments(
        @Path("postId") postId: String
    ): Response<ApiResponse>

    @POST("posts/add_comment/{postId}")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body comment: Comment
    ): Response<ApiResponse>

    @POST("register/{login}/{password}/{username}")
    suspend fun registerUser(
        @Path("login") login: String,
        @Path("password") password: String,
        @Path("username") username: String
    ): Response<ApiResponse>
}
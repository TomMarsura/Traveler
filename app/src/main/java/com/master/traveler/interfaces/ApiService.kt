package com.master.traveler.interfaces

import com.master.traveler.data.ApiResponse
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
}
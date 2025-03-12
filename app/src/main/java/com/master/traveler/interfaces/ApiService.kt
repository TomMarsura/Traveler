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

    @GET("posts/get_from_user/{userId}")
    suspend fun getPostsFromUser(
        @Path("userId") userId: String
    ): Response<ApiResponse>

    @GET("users/get_infos/{userId}")
    suspend fun getUserInfos(
        @Path("userId") userId: String
    ): Response<ApiResponse>

    @POST("users/follow/{currentUserId}/{otherUserId}/{isFollowed}")
    suspend fun followUser(
        @Path("currentUserId") currentUserId: String,
        @Path("otherUserId") otherUserId: String,
        @Path("isFollowed") isFollowed: Boolean
    ): Response<ApiResponse>

    @GET("users/get_profile_picture/{userId}")
    suspend fun getProfilePicture(
        @Path("userId") userId: String
    ): Response<ApiResponse>
}
package com.master.traveler.interfaces

import com.master.traveler.data.ApiResponse
import com.master.traveler.data.Comment
import com.master.traveler.data.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("posts/add")
    suspend fun addPost(@Body post: Post): Response<ApiResponse>

    @GET("posts")
    suspend fun getPosts(): Response<ApiResponse>

    @GET("posts/followed/{userId}")
    suspend fun getFollowedPosts(
        @Path("userId") userId: String
    ): Response<ApiResponse>

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
      
    @POST("register/{login}/{password}/{username}")
    suspend fun registerUser(
        @Path("login") login: String,
        @Path("password") password: String,
        @Path("username") username: String
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

    @POST("users/update_login/{userId}/{newLogin}")
    suspend fun updateLogin(
        @Path("userId") userId: String,
        @Path("newLogin") newLogin: String
    ): Response<ApiResponse>

    @POST("users/update_username/{userId}/{newUsername}")
    suspend fun updateUsername(
        @Path("userId") userId: String,
        @Path("newUsername") newUsername: String
    ): Response<ApiResponse>

    @POST("users/update_password/{userId}/{newPassword}")
    suspend fun updatePassword(
        @Path("userId") userId: String,
        @Path("newPassword") newPassword: String
    ): Response<ApiResponse>

    @POST("users/update_bio/{userId}/{newBio}")
    suspend fun updateBio(
        @Path("userId") userId: String,
        @Path("newBio") newBio: String
    ): Response<ApiResponse>

    @POST("users/update_profile_picture/{userId}")
    suspend fun updateProfileImage(
        @Path("userId") userId: String,
        @Body imageUrl: Map<String, String>
    ): Response<ApiResponse>

    @GET("config/get_profile_pictures")
    suspend fun getProfileImages(): Response<ApiResponse>

}
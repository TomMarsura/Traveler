package com.master.traveler.data

data class ApiResponse(
    val code: Int,
    val posts: List<PostHome>,
    val nb_likes: Int,
    val comments: List<Comment>,
    val nb_comments: Int,
    val user: User,
    val profilePicture: String,
    val images: List<String>,
)

data class PostHome(
    val id: String,
    val user_id: String,
    val name: String,
    val total_price: Int,
    val nb_comments: Int,
    val presentation: Presentation,
    val likes: Int,
    val user_name: String
)

data class Post(
    val id: String,
    val user_id: String,
    val name: String,
    val post_date: String,
    val travel_infos: TravelInfos,
    val total_price: Int,
    val presentation: Presentation,
    val nb_comments: Int,
    val comments: List<Comment>,
    val likes: Int,
    val user_name: String
)

data class TravelInfos(
    val arrival_date: String,
    val departure_date: String,
    val airplane: Boolean,
    val company_infos: CompanyInfos,
    val activities: List<Activity>,
    val accommodations: List<Accommodation>,
    val pictures: List<Picture>,
    val description: String
)

data class CompanyInfos(
    val name: String,
    val price: Float,
    val flight_link: String
)

data class Activity(
    val name: String,
    val price: Float,
    val address: String
)

data class Accommodation(
    val food: List<Food>,
    val hotel: List<Hotel>,
    val transport: List<Transport>
)

data class Food(
    val name: String,
    val price: Float
)

data class Hotel(
    val name: String,
    val price: Float
)

data class Transport(
    val name: String,
    val price: Float
)

data class Picture(
    val url: String
)

data class Presentation(
    val image: String,
    val total_time: Int,
    val card_color: String,
    val text_color: String
)

data class Comment(
    val user_id: String,
    val login: String,
    val comment: String
)

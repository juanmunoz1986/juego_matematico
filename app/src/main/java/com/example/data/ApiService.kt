package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    val access_token: String,
    val refresh_token: String?,
    val user: SupabaseUser
)

@JsonClass(generateAdapter = true)
data class SupabaseScoreRequest(
    val player_id: String,
    val score: Int,
    val max_streak: Int,
    val difficulty: String,
    // La columna en la tabla `scores` se llama `level_reached`
    @Json(name = "level_reached") val level: Int
)

@JsonClass(generateAdapter = true)
data class SupabaseLeaderboardItem(
    val id: Long? = null,
    val score: Int = 0,
    val max_streak: Int? = 0,
    val difficulty: String? = "",
    // La vista `leaderboard` expone `level_reached` y `player_name`
    @Json(name = "level_reached") val level: Int? = 1,
    @Json(name = "player_name") val display_name: String? = "Invitado",
    val created_at: String? = null
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val refresh_token: String
)

@JsonClass(generateAdapter = true)
data class SupabasePlayerProfile(
    val display_name: String
)

interface ApiService {
    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    suspend fun signUpAnonymous(
        @Header("apikey") apiKey: String,
        @Body body: Map<String, String> = emptyMap()
    ): SupabaseAuthResponse

    @POST("auth/v1/token?grant_type=refresh_token")
    @Headers("Content-Type: application/json")
    suspend fun refreshToken(
        @Header("apikey") apiKey: String,
        @Body body: RefreshTokenRequest
    ): SupabaseAuthResponse

    @POST("rest/v1/scores")
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    suspend fun submitScore(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body score: SupabaseScoreRequest
    ): Response<List<Map<String, Any>>>

    @GET("rest/v1/leaderboard")
    @Headers("Content-Type: application/json")
    suspend fun getLeaderboard(
        @Header("apikey") apiKey: String,
        @Query("limit") limit: Int = 50,
        @Query("order") orderBy: String = "score.desc"
    ): List<SupabaseLeaderboardItem>

    @PATCH("rest/v1/players")
    @Headers("Content-Type: application/json")
    suspend fun updatePlayerProfile(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String, // format: eq.<uuid>
        @Body profile: SupabasePlayerProfile
    ): Response<Unit>
}

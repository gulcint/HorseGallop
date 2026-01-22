package com.horsegallop.data.remote

import com.horsegallop.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
  @POST("auth/google")
  suspend fun postAuthGoogle(@Body body: AuthRequestDto): AuthResponseDto

  @POST("auth/apple")
  suspend fun postAuthApple(@Body body: AuthRequestDto): AuthResponseDto

  @GET("slider")
  suspend fun getSlider(): List<SliderItemDto>

  // Auth
  @POST("auth/login")
  suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

  @POST("auth/register")
  suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

  // User
  @GET("users/me")
  suspend fun getProfile(): UserProfileDto

  @PUT("users/me")
  suspend fun updateProfile(@Body body: UpdateProfileRequestDto): UserProfileDto

  @GET("users/me/stats")
  suspend fun getUserStats(): UserStatsDto

  // Barns
  @GET("barns")
  suspend fun getBarns(
      @retrofit2.http.Query("lat") lat: Double?,
      @retrofit2.http.Query("lng") lng: Double?,
      @retrofit2.http.Query("radius") radiusKm: Double?
  ): List<BarnDto>

  @GET("barns/{id}")
  suspend fun getBarnDetail(@retrofit2.http.Path("id") id: String): BarnDetailDto

  // Rides
  @GET("rides")
  suspend fun getRideHistory(
      @retrofit2.http.Query("limit") limit: Int = 20,
      @retrofit2.http.Query("offset") offset: Int = 0
  ): List<RideSessionDto>

  @POST("rides")
  suspend fun createRide(@Body body: CreateRideRequestDto): RideSessionDto

  // Schedule
  @GET("lessons")
  suspend fun getLessons(
      @retrofit2.http.Query("from") fromDate: String?, // ISO 8601
      @retrofit2.http.Query("to") toDate: String? // ISO 8601
  ): List<LessonDto>
}

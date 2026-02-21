package com.horsegallop.data.remote

import com.horsegallop.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.ResponseBody

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

  // KVKK
  @GET("users/export")
  suspend fun exportUserData(): ResponseBody

  @POST("users/delete")
  suspend fun deleteUserData(): ResponseBody

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

  // Ride tracking (backend v2)
  @POST("rides/start")
  suspend fun startRide(@Body body: StartRideRequestDto): StartRideResponseDto

  @POST("rides/{id}/stop")
  suspend fun stopRide(
      @Path("id") id: String,
      @Body body: StopRideRequestDto
  ): StopRideResponseDto

  // Schedule
  @GET("lessons")
  suspend fun getLessons(
      @retrofit2.http.Query("from") fromDate: String?, // ISO 8601
      @retrofit2.http.Query("to") toDate: String? // ISO 8601
  ): List<LessonDto>

  // Backend v2 endpoints
  @GET("barns")
  suspend fun getBarnsV2(): List<BackendBarnDto>

  @GET("barns/{id}")
  suspend fun getBarnDetailV2(@Path("id") id: String): BackendBarnDto

  @GET("lessons")
  suspend fun getLessonsV2(): List<BackendLessonDto>

  @GET("rides/me")
  suspend fun getMyRidesV2(): List<BackendRideDto>
}

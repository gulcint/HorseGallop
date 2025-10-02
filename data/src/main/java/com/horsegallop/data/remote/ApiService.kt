package com.horsegallop.data.remote

import com.horsegallop.data.remote.dto.AuthRequestDto
import com.horsegallop.data.remote.dto.AuthResponseDto
import com.horsegallop.data.remote.dto.SliderItemDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
  @POST("auth/google")
  suspend fun postAuthGoogle(@Body body: AuthRequestDto): AuthResponseDto

  @POST("auth/apple")
  suspend fun postAuthApple(@Body body: AuthRequestDto): AuthResponseDto

  @GET("slider")
  suspend fun getSlider(): List<SliderItemDto>
}

package com.horsegallop.data.repository

import com.horsegallop.data.remote.ApiService
import com.horsegallop.domain.model.SliderItem
import com.horsegallop.domain.repository.HomeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HomeRepositoryImpl @Inject constructor(
  private val api: ApiService
) : HomeRepository {

  override fun getSlider(): Flow<Result<List<SliderItem>>> {
    return flow {
      try {
        val response = api.getSlider()
        val sliderItems = response.map { dto ->
          SliderItem(
            id = dto.id,
            imageUrl = dto.imageUrl,
            title = dto.title,
            link = dto.link,
            order = dto.order
          )
        }
        emit(Result.success(sliderItems))
      } catch (e: Exception) {
        emit(Result.failure(e))
      }
    }
  }
}

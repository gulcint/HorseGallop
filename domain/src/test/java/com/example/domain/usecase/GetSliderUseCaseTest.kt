package com.example.domain.usecase

import com.example.domain.model.SliderItem
import com.example.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSliderUseCaseTest {
  @Test
  fun returnsSuccess() = runTest {
    val repo = object : HomeRepository {
      override fun getSlider(): Flow<Result<List<SliderItem>>> = flow {
        emit(Result.success(listOf(SliderItem("s1", "url", "t", null, 1))))
      }
    }
    val useCase = GetSliderUseCase(repo)
    val result = useCase().first()
    assertTrue(result.isSuccess)
  }
}

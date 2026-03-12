package com.horsegallop.data.horse.repository

import com.horsegallop.data.remote.dto.HorseFunctionsDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.domain.horse.model.HorseTip
import com.horsegallop.domain.horse.repository.HorseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject

class HorseRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : HorseRepository {

    override fun getMyHorses(): Flow<List<Horse>> = flow {
        try {
            val horses = functionsDataSource.getMyHorses().map { it.toDomain() }
            emit(horses)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addHorse(horse: Horse): Result<Horse> = runCatching {
        functionsDataSource.addHorse(
            name = horse.name,
            breed = horse.breed,
            birthYear = horse.birthYear,
            color = horse.color,
            gender = horse.gender.name.lowercase(),
            weightKg = horse.weightKg
        ).toDomain()
    }

    override suspend fun deleteHorse(horseId: String): Result<Unit> = runCatching {
        functionsDataSource.deleteHorse(horseId)
    }

    override suspend fun getBreeds(locale: String): Result<List<String>> = runCatching {
        val dtos = functionsDataSource.getBreeds(locale)
        val isTurkish = locale.startsWith("tr", ignoreCase = true)
        dtos.map { breed ->
            if (isTurkish && breed.nameTr.isNotBlank()) breed.nameTr else breed.nameEn
        }
    }

    override suspend fun getHorseTips(locale: String): Result<List<HorseTip>> = runCatching {
        functionsDataSource.getHorseTips(locale).map { dto ->
            HorseTip(id = dto.id, title = dto.title, body = dto.body, category = dto.category)
        }
    }

    private fun HorseFunctionsDto.toDomain() = Horse(
        id = id,
        name = name,
        breed = breed,
        birthYear = birthYear,
        color = color,
        gender = when (gender.lowercase()) {
            "stallion" -> HorseGender.STALLION
            "mare" -> HorseGender.MARE
            "gelding" -> HorseGender.GELDING
            else -> HorseGender.UNKNOWN
        },
        weightKg = weightKg,
        imageUrl = imageUrl
    )
}

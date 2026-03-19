package com.horsegallop.data.horse.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseHorseDto
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.domain.horse.model.HorseTip
import com.horsegallop.domain.horse.repository.HorseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HorseRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : HorseRepository {

    override fun getMyHorses(): Flow<List<Horse>> = flow {
        emit(supabaseDataSource.getMyHorses().map { it.toDomain() })
    }.catch { emit(emptyList()) }

    override suspend fun addHorse(horse: Horse): Result<Horse> = runCatching {
        val dto = SupabaseHorseDto(
            name = horse.name,
            breed = horse.breed,
            birthYear = horse.birthYear,
            color = horse.color,
            gender = horse.gender.name.lowercase(),
            weightKg = horse.weightKg,
            imageUrl = horse.imageUrl ?: ""
        )
        supabaseDataSource.addHorse(dto).toDomain()
    }

    override suspend fun deleteHorse(horseId: String): Result<Unit> = runCatching {
        supabaseDataSource.deleteHorse(horseId)
    }

    override suspend fun getBreeds(locale: String): Result<List<String>> = runCatching {
        val dtos = supabaseDataSource.getBreeds()
        val isTurkish = locale.startsWith("tr", ignoreCase = true)
        dtos.map { breed ->
            if (isTurkish && breed.nameTr.isNotBlank()) breed.nameTr else breed.nameEn
        }
    }

    override suspend fun getHorseTips(locale: String): Result<List<HorseTip>> = runCatching {
        supabaseDataSource.getHorseTips(locale).map { dto ->
            HorseTip(id = dto.id, title = dto.title, body = dto.body, category = dto.category)
        }
    }

    private fun SupabaseHorseDto.toDomain() = Horse(
        id = id,
        name = name,
        breed = breed,
        birthYear = birthYear ?: 0,
        color = color,
        gender = when (gender.lowercase()) {
            "stallion" -> HorseGender.STALLION
            "mare" -> HorseGender.MARE
            "gelding" -> HorseGender.GELDING
            else -> HorseGender.UNKNOWN
        },
        weightKg = weightKg ?: 0,
        imageUrl = imageUrl
    )
}

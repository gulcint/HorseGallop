package com.horsegallop.data.barn.repository

import com.horsegallop.domain.barn.model.BarnUi
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.domain.barn.repository.BarnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarnRepositoryImpl @Inject constructor() : BarnRepository {

    private val _barns = MutableStateFlow(
        listOf(
            BarnWithLocation(
                barn = BarnUi(
                    id = "1", 
                    name = "Adin Country", 
                    description = "Beginner to Pro rides",
                    location = "Istanbul, TR",
                    tags = listOf("cafe", "indoor_arena", "parking", "lessons", "lighting", "open_now")
                ),
                lat = 41.0082, lng = 28.9784,
                amenities = setOf("cafe", "indoor_arena", "parking", "lessons", "lighting", "open_now")
            ),
            BarnWithLocation(
                barn = BarnUi(
                    id = "2", 
                    name = "Sable Ranch", 
                    description = "Trail and endurance",
                    location = "Sariyer, TR",
                    tags = listOf("outdoor_arena", "trail", "parking", "boarding", "farrier")
                ),
                lat = 41.0151, lng = 29.0037,
                amenities = setOf("outdoor_arena", "trail", "parking", "boarding", "farrier")
            ),
            BarnWithLocation(
                barn = BarnUi(
                    id = "3", 
                    name = "Silver Hoof", 
                    description = "Dressage & Jumping",
                    location = "Kemerburgaz, TR",
                    tags = listOf("indoor_arena", "outdoor_arena", "lessons", "vet", "parking", "open_now")
                ),
                lat = 41.0258, lng = 29.0150,
                amenities = setOf("indoor_arena", "outdoor_arena", "lessons", "vet", "parking", "open_now")
            )
        )
    )

    override fun getBarns(): Flow<List<BarnWithLocation>> = _barns

    override fun getBarnById(barnId: String): Flow<BarnWithLocation?> = _barns.map { list ->
        list.find { it.barn.id == barnId }
    }

    override suspend fun toggleFavorite(barnId: String) {
        _barns.update { currentList ->
            currentList.map { barnWithLoc ->
                if (barnWithLoc.barn.id == barnId) {
                    barnWithLoc.copy(
                        barn = barnWithLoc.barn.copy(isFavorite = !barnWithLoc.barn.isFavorite)
                    )
                } else {
                    barnWithLoc
                }
            }
        }
    }
}

package com.horsegallop.data.remote.dto

data class BarnDto(
    val id: String,
    val name: String,
    val description: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val rating: Double?,
    val tags: List<String>?,
    val isFavorite: Boolean?
)

data class BarnDetailDto(
    val id: String,
    val name: String,
    val description: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val images: List<String>?,
    val rating: Double?,
    val reviewCount: Int?,
    val tags: List<String>?,
    val amenities: List<String>?,
    val phone: String?,
    val website: String?,
    val email: String?,
    val isFavorite: Boolean?
)

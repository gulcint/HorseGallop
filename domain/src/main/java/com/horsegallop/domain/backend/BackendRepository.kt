package com.horsegallop.domain.backend

import kotlinx.coroutines.flow.Flow

interface BackendRepository {
    // User operations
    suspend fun getUser(userId: String): Flow<Result<User>>
    suspend fun updateUser(userId: String, userData: Map<String, Any?>): Flow<Result<User>>
    
    // Horse operations
    suspend fun getHorses(userId: String): Flow<Result<List<Horse>>>
    suspend fun addHorse(userId: String, horse: Horse): Flow<Result<Horse>>
    
    // Session operations
    suspend fun getSessions(userId: String, limit: Int = 50, offset: Int = 0): Flow<Result<List<Session>>>
    suspend fun createSession(userId: String, session: Session): Flow<Result<Session>>
}

// Data models
data class User(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val birthDate: String? = null,
    val photoUrl: String? = null,
    val countryCode: String? = null,
    val createdAt: String? = null
)

data class Horse(
    val id: String = "",
    val userId: String,
    val name: String,
    val breed: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val color: String? = null,
    val photoUrl: String? = null,
    val notes: String? = null
)

data class Session(
    val id: String = "",
    val userId: String,
    val horseId: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Float? = null, // in meters
    val duration: Int? = null, // in seconds
    val rideType: String? = null,
    val notes: String? = null,
    val createdAt: Long? = null
)

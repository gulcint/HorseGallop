package com.horsegallop.feature.home.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.horsegallop.feature.home.domain.model.RideSession
import com.horsegallop.feature.home.domain.model.UserStats
import com.horsegallop.feature.home.domain.repository.HomeRepository
import com.horsegallop.data.remote.ApiService
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class HomeRepositoryImpl @Inject constructor(
  private val api: ApiService,
  private val firestore: FirebaseFirestore
) : HomeRepository {

  override fun getRecentActivities(userId: String, limit: Int): Flow<Result<List<RideSession>>> = flow {
    try {
      val snapshot = firestore.collection("users")
        .document(userId)
        .collection("rides")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(limit.toLong())
        .get()
        .await()

      val activities = snapshot.documents.map { doc ->
        RideSession(
          id = doc.id,
          title = doc.getString("title") ?: "Ride",
          timestamp = doc.getTimestamp("timestamp")?.toDate(),
          durationMin = (doc.getLong("durationMin") ?: 0L).toInt(),
          distanceKm = doc.getDouble("distanceKm") ?: (doc.getLong("distanceKm")?.toDouble() ?: 0.0)
        )
      }
      emit(Result.success(activities))
    } catch (e: Exception) {
      emit(Result.failure(e))
    }
  }

  override fun getUserStats(userId: String): Flow<Result<UserStats>> = flow {
    try {
      val snapshot = firestore.collection("users")
        .document(userId)
        .collection("rides")
        .get()
        .await()

      val count = snapshot.size()
      val dist = snapshot.documents.sumOf {
        it.getDouble("distanceKm") ?: (it.getLong("distanceKm")?.toDouble() ?: 0.0)
      }
      
      emit(Result.success(UserStats(count, dist)))
    } catch (e: Exception) {
      emit(Result.failure(e))
    }
  }
}

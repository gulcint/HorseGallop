package com.horsegallop.data.ride.repository

import android.content.Context
import com.horsegallop.domain.ride.model.RideSession
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RideHistoryRepository {
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, RideSession::class.java)
    private val adapter = moshi.adapter<List<RideSession>>(listType)
    private val file by lazy { File(context.filesDir, "ride_history.json") }
    
    private val _history = MutableStateFlow<List<RideSession>>(emptyList())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (file.exists()) {
                try {
                    val json = file.readText()
                    val list = adapter.fromJson(json) ?: emptyList()
                    _history.value = list
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getRideHistory(): Flow<List<RideSession>> = _history

    override fun getRide(id: String): Flow<RideSession?> = _history.map { list ->
        list.find { it.id == id }
    }

    override suspend fun saveRide(ride: RideSession) {
        val updatedList = listOf(ride) + _history.value
        _history.value = updatedList
        withContext(Dispatchers.IO) {
            try {
                val json = adapter.toJson(updatedList)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.horsegallop.data.ride.repository

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class RideSyncOutboxStore private constructor(
    private val file: File,
    private val ioDispatcher: CoroutineDispatcher
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        file = File(context.filesDir, OUTBOX_FILE_NAME),
        ioDispatcher = Dispatchers.IO
    )

    internal constructor(file: File) : this(
        file = file,
        ioDispatcher = Dispatchers.IO
    )

    private val mutex = Mutex()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, PendingRideStopSync::class.java)
    private val adapter = moshi.adapter<List<PendingRideStopSync>>(listType)
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    init {
        _pendingCount.value = runCatching {
            if (!file.exists()) return@runCatching 0
            val json = file.readText()
            adapter.fromJson(json).orEmpty().size
        }.getOrDefault(0)
    }

    suspend fun append(entry: PendingRideStopSync): String = mutateList { current ->
        current + entry
    }.let { entry.id }

    suspend fun listDue(nowMillis: Long): List<PendingRideStopSync> = withContext(ioDispatcher) {
        mutex.withLock {
            readAllUnsafe().filter { it.nextRetryAtMillis <= nowMillis }
        }
    }

    suspend fun markSynced(outboxId: String) {
        mutateList { current -> current.filterNot { it.id == outboxId } }
    }

    suspend fun rescheduleFailed(
        outboxId: String,
        retryCount: Int,
        nextRetryAtMillis: Long,
        errorMessage: String?
    ) {
        mutateList { current ->
            current.map { item ->
                if (item.id != outboxId) {
                    item
                } else {
                    item.copy(
                        retryCount = retryCount,
                        nextRetryAtMillis = nextRetryAtMillis,
                        lastErrorMessage = errorMessage
                    )
                }
            }
        }
    }

    suspend fun purgeExpired(nowMillis: Long, maxAgeMillis: Long) {
        mutateList { current ->
            current.filter { item -> (nowMillis - item.createdAtMillis) <= maxAgeMillis }
        }
    }

    internal suspend fun snapshot(): List<PendingRideStopSync> = withContext(ioDispatcher) {
        mutex.withLock { readAllUnsafe() }
    }

    private suspend fun mutateList(
        transform: (List<PendingRideStopSync>) -> List<PendingRideStopSync>
    ): List<PendingRideStopSync> = withContext(ioDispatcher) {
        mutex.withLock {
            val updated = transform(readAllUnsafe())
            writeAllUnsafe(updated)
            updated
        }
    }

    private fun readAllUnsafe(): List<PendingRideStopSync> {
        if (!file.exists()) return emptyList()
        val json = file.readText()
        return adapter.fromJson(json).orEmpty()
    }

    private fun writeAllUnsafe(entries: List<PendingRideStopSync>) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        file.writeText(adapter.toJson(entries))
        _pendingCount.value = entries.size
    }

    private companion object {
        const val OUTBOX_FILE_NAME = "ride_sync_outbox.json"
    }
}

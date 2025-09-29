package com.example.core.localization

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DynamicStringProvider(
  private val context: Context
) {
  private var remote: Map<String, String> = emptyMap()

  suspend fun loadFromJson(json: String) {
    val map: MutableMap<String, String> = mutableMapOf()
    withContext(Dispatchers.Default) {
      val root = JSONObject(json)
      val keys = root.keys()
      while (keys.hasNext()) {
        val key = keys.next()
        map[key] = root.getString(key)
      }
    }
    remote = map
  }

  fun getString(key: String, fallbackResId: Int): String {
    val value: String? = remote[key]
    if (value != null) return value
    return context.getString(fallbackResId)
  }
}

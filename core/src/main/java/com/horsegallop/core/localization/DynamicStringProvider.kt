package com.horsegallop.core.localization

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DynamicStringProvider(
  private val context: Context
) {
  private var remoteByEntryName: Map<String, String> = emptyMap()

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
    remoteByEntryName = map
  }

  fun getString(@androidx.annotation.StringRes resId: Int): String {
    val entryName: String = context.resources.getResourceEntryName(resId)
    val value: String? = remoteByEntryName[entryName]
    if (value != null) return value
    return context.getString(resId)
  }
}

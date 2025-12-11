package com.horsegallop.core.debug

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LogBuffer {
  private const val CAPACITY = 1000
  private val _logs = MutableStateFlow<List<String>>(emptyList())
  val logs: StateFlow<List<String>> = _logs

  @Synchronized
  fun append(line: String) {
    val current = _logs.value
    val next = if (current.size >= CAPACITY) (current.drop(current.size - CAPACITY + 1) + line) else (current + line)
    _logs.value = next
  }

  fun clear() {
    _logs.value = emptyList()
  }
}


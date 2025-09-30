package com.example.core.error

import android.util.Log

/**
 * Global exception handler for debug builds only
 */
class GlobalExceptionHandler private constructor(
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val isDebug: Boolean
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (isDebug) {
            Log.e(TAG, "═══════════════════════════════════════════════════════════════")
            Log.e(TAG, "UNCAUGHT EXCEPTION IN THREAD: ${thread.name}")
            Log.e(TAG, "═══════════════════════════════════════════════════════════════")
            Log.e(TAG, "Exception: ${throwable.javaClass.simpleName}")
            Log.e(TAG, "Message: ${throwable.message}")
            Log.e(TAG, "───────────────────────────────────────────────────────────────")
            Log.e(TAG, "Stack trace:")
            throwable.printStackTrace()
            Log.e(TAG, "═══════════════════════════════════════════════════════════════")
        }
        
        // Pass to default handler (will crash the app)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        
        fun install(isDebug: Boolean) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is GlobalExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    GlobalExceptionHandler(defaultHandler, isDebug)
                )
            }
        }
    }
}

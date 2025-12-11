package com.horsegallop.core.debug

import android.util.Log

object AppLog {
    private const val GLOBAL_TAG = "horseGallopTest"

    fun d(tag: String, msg: String) {
        Log.d(GLOBAL_TAG, "[$tag] $msg")
    }
    fun i(tag: String, msg: String) {
        Log.i(GLOBAL_TAG, "[$tag] $msg")
    }
    fun w(tag: String, msg: String) {
        Log.w(GLOBAL_TAG, "[$tag] $msg")
    }
    fun e(tag: String, msg: String) {
        Log.e(GLOBAL_TAG, "[$tag] $msg")
    }
}

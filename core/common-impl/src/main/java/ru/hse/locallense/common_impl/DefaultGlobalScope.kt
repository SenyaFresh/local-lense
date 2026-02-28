package ru.hse.locallense.common_impl

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates global coroutine scope with error handler, supervisor job object and main dispatcher.
 */
fun createDefaultGlobalScope(): CoroutineScope {
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("DefaultGlobalScope", "Error", throwable)
    }

    return CoroutineScope(SupervisorJob() + Dispatchers.Main + errorHandler)
}
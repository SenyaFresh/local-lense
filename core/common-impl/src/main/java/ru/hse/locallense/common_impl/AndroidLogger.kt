package ru.hse.locallense.common_impl

import android.util.Log
import ru.hse.locallense.common.Logger

class AndroidLogger : Logger {

    override fun log(message: String) {
        Log.d("AndroidLogger", message)
    }

    override fun logError(exception: Throwable, message: String?) {
        Log.d("AndroidLogger", message, exception)
    }

}
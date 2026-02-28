package ru.hse.locallense.common

interface Logger {

    /**
     * Log some [message].
     */
    fun log(message: String)

    /**
     * Log some [error].
     */
    fun logError(exception: Throwable, message: String? = null)
}
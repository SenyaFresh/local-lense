package ru.hse.locallense.common

interface Logger {

    fun log(message: String)

    fun logError(exception: Throwable, message: String? = null)
}
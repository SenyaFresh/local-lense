package ru.hse.locallense.common

interface ErrorHandler {

    fun handleError(exception: Throwable)

    fun getUserFriendlyMessage(exception: Throwable): String
}
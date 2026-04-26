package ru.hse.locallense.common

open class AppException(
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

open class UserFriendlyException(
    val userFriendlyMessage: String,
    cause: Exception? = null
) : AppException(cause?.message ?: "", cause)

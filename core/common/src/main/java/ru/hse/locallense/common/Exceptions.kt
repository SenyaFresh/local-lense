package ru.hse.locallense.common

/**
 * Exception that is handled by application.
 */
open class AppException(
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception with message that can be shown to user.
 */
open class UserFriendlyException(
    val userFriendlyMessage: String,
    cause: Exception? = null
) : AppException(cause?.message ?: "", cause)

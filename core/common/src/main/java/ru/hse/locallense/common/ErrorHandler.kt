package ru.hse.locallense.common

/**
 * Error handler for actions.
 */
interface ErrorHandler {

    /**
     * Handle [exception].
     */
    fun handleError(exception: Throwable)

    /**
     * Get user-friendly message from [exception]
     */
    fun getUserFriendlyMessage(exception: Throwable) : String
}
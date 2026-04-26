package ru.hse.locallense.common_impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import ru.hse.locallense.common.ErrorHandler
import ru.hse.locallense.common.Logger
import ru.hse.locallense.common.Resources
import ru.hse.locallense.common.Toaster
import ru.hse.locallense.common.UserFriendlyException

class DefaultErrorHandler(
    private val logger: Logger,
    private val resources: Resources,
    private val toaster: Toaster
) : ErrorHandler {

    override fun handleError(exception: Throwable) {
        logger.logError(exception)
        when (exception) {
            is UserFriendlyException -> handleUserFriendlyException(exception)
            is TimeoutCancellationException -> handleTimeoutCancellationException(exception)
            is CancellationException -> return
            else -> handleUnknownException(exception)
        }
    }

    override fun getUserFriendlyMessage(exception: Throwable): String {
        return when (exception) {
            is UserFriendlyException -> exception.userFriendlyMessage
            is TimeoutCancellationException -> resources.getString(R.string.core_common_exception_timeout)
            else -> resources.getString(R.string.core_common_exception_unknown)
        }
    }

    private fun handleUserFriendlyException(exception: UserFriendlyException) {
        toaster.showToast(getUserFriendlyMessage(exception))
    }

    private fun handleTimeoutCancellationException(exception: TimeoutCancellationException) {
        toaster.showToast(getUserFriendlyMessage(exception))
    }

    private fun handleUnknownException(exception: Throwable) {
        toaster.showToast(getUserFriendlyMessage(exception))
    }

}
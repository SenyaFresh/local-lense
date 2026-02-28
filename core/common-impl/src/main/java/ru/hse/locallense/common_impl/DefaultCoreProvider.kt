package ru.hse.locallense.common_impl

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import ru.hse.locallense.common.CoreProvider
import ru.hse.locallense.common.ErrorHandler
import ru.hse.locallense.common.Logger
import ru.hse.locallense.common.Resources
import ru.hse.locallense.common.Toaster

class DefaultCoreProvider(
    private val appContext: Context,
    override val resources: Resources = AndroidResources(appContext),
    override val globalScope: CoroutineScope = createDefaultGlobalScope(),
    override val toaster: Toaster = AndroidToaster(appContext),
    override val logger: Logger = AndroidLogger(),
    override val errorHandler: ErrorHandler = DefaultErrorHandler(
        logger,
        resources,
        toaster
    )
) : CoreProvider
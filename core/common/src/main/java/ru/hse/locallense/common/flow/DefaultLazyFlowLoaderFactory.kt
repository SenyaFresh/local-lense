package ru.hse.locallense.common.flow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import ru.hse.locallense.common.Core

class DefaultLazyFlowLoaderFactory(
    private val dispatcher: CoroutineDispatcher,
    private val globalScope: CoroutineScope = Core.globalScope,
    private val cacheTimeoutMillis: Long = 1000,
    private val loadingStatusTimeoutMillis: Long = 100
) : LazyFlowLoaderFactory {

    override fun <T> create(loader: ValueLoader<T>): LazyFlowLoader<T> {
        return DefaultLazyFlowLoader(
            loader,
            dispatcher,
            globalScope,
            cacheTimeoutMillis,
            loadingStatusTimeoutMillis
        )
    }

}
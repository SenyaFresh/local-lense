package ru.hse.locallense.common.flow

import kotlinx.coroutines.flow.Flow
import ru.hse.locallense.common.ResultContainer

/**
 * Async container based on [Flow] for loading data and listening current status of loading.
 */

typealias ValueLoader<T> = suspend () -> T

interface LazyFlowLoader<T> {

    /**
     * Listen for loaded values.
     */
    fun listen(): Flow<ResultContainer<T>>

    /**
     * Start a new load and wait for result. Use [silently] if [ResultContainer] should not
     * be in [ResultContainer.Loading] status.
     */
    suspend fun newLoad(silently: Boolean = false, valueLoader: ValueLoader<T>? = null): T

    /**
     * Start a new load without waiting for a result. Use [silently] if [ResultContainer] should not
     * be in [ResultContainer.Loading] status.
     */
    fun newAsyncLoad(silently: Boolean = false, valueLoader: ValueLoader<T>? = null)

    /**
     * Update value immediately with new [ResultContainer].
     */
    fun update(container: ResultContainer<T>)

    /**
     * Update value immediately using [updater].
     */
    fun update(updater: (ResultContainer<T>) -> ResultContainer<T>)

}
package ru.hse.locallense.common.flow

import kotlinx.coroutines.flow.Flow
import ru.hse.locallense.common.ResultContainer

typealias ValueLoader<T> = suspend () -> T

interface LazyFlowLoader<T> {

    fun listen(): Flow<ResultContainer<T>>

    suspend fun newLoad(silently: Boolean = false, valueLoader: ValueLoader<T>? = null): T

    fun newAsyncLoad(silently: Boolean = false, valueLoader: ValueLoader<T>? = null)

    fun update(container: ResultContainer<T>)

    fun update(updater: (ResultContainer<T>) -> ResultContainer<T>)

}
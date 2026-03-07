package ru.hse.locallense.common.flow

/**
 * Factory for [LazyFlowLoader].
 */
interface LazyFlowLoaderFactory {

    /**
     * Create a new instance of [LazyFlowLoader].
     */
    fun <T> create(loader: ValueLoader<T>): LazyFlowLoader<T>
}
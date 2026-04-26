package ru.hse.locallense.common.flow

interface LazyFlowLoaderFactory {

    fun <T> create(loader: ValueLoader<T>): LazyFlowLoader<T>
}
package ru.hse.locallense.common

import kotlinx.coroutines.runBlocking

sealed class ResultContainer<out T> {

    fun <R> map(mapper: ((T) -> R)? = null): ResultContainer<R> {
        return runBlocking {
            val suspendMapper: (suspend (T) -> R)? = if (mapper == null) {
                null
            } else {
                {
                    mapper(it)
                }
            }
            suspendMap(suspendMapper)
        }
    }

    companion object {
        fun wrap(vararg values: ResultContainer<Any>): ResultContainer<Unit> {
            return values.fold(Done(Unit) as ResultContainer<Unit>) { accumulator, value ->
                when (value) {
                    is Loading -> Loading
                    is Error -> Error(value.exception)
                    else -> accumulator
                }
            }
        }
    }

    protected abstract suspend fun <R> suspendMap(mapper: (suspend (T) -> R)? = null): ResultContainer<R>

    abstract fun unwrap(): T

    abstract fun unwrapOrNull(): T?

    data object Loading : ResultContainer<Nothing>() {
        override suspend fun <R> suspendMap(mapper: (suspend (Nothing) -> R)?): ResultContainer<R> {
            return this
        }

        override fun unwrap(): Nothing {
            throw IllegalStateException("Can't unwrap, result is pending.")
        }

        override fun unwrapOrNull(): Nothing? {
            return null
        }
    }

    data class Error(
        val exception: Exception
    ) : ResultContainer<Nothing>() {
        override suspend fun <R> suspendMap(mapper: (suspend (Nothing) -> R)?): ResultContainer<R> {
            return this
        }

        override fun unwrap(): Nothing {
            throw exception
        }

        override fun unwrapOrNull(): Nothing? {
            return null
        }
    }

    data class Done<T>(
        val value: T
    ) : ResultContainer<T>() {
        override suspend fun <R> suspendMap(mapper: (suspend (T) -> R)?): ResultContainer<R> {
            if (mapper == null) throw IllegalStateException("Can't map value: mapper is null.")
            return try {
                Done(mapper(value))
            } catch (e: Exception) {
                Error(e)
            }
        }

        override fun unwrap(): T {
            return value
        }

        override fun unwrapOrNull(): T? {
            return value
        }
    }
}
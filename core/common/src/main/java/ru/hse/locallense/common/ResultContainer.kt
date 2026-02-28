package ru.hse.locallense.common

import kotlinx.coroutines.runBlocking

/**
 * Container for current status of async operation.
 *
 * Contains [map] method for converting data and [suspendMap] method for converting data
 * from a coroutine.
 *
 * @see ResultContainer.Loading
 * @see ResultContainer.Error
 * @see ResultContainer.Done
 */
sealed class ResultContainer<out T> {

    /**
     * Convert ResultContainer type to another type using specified [mapper].
     */
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
        /**
         * Wrap specified containers into one.
         */
        fun wrap(vararg values: ResultContainer<Any>): ResultContainer<Unit> {
            return values.fold(Done(Unit) as ResultContainer<Unit>) { acc, value ->
                when (value) {
                    is Loading -> Loading
                    is Error -> Error(value.exception)
                    else -> acc
                }
            }
        }
    }

    /**
     * Convert ResultContainer type to another type using specified suspend [mapper].
     */
    protected abstract suspend fun <R> suspendMap(mapper: (suspend (T) -> R)? = null): ResultContainer<R>

    /**
     * Get value of ReturnContainer if it is possible or throw an exception.
     */
    abstract fun unwrap(): T

    /**
     * Get value of ReturnContainer if it is possible or null.
     */
    abstract fun unwrapOrNull(): T?

    /**
     * Operation in progress.
     */
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

    /**
     * Operation failed.
     */
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

    /**
     * Operation was completed successfully.
     */
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
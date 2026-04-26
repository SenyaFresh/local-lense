package ru.hse.locallense.common.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.hse.locallense.common.ResultContainer

class DefaultLazyFlowLoader<T>(
    private var valueLoader: ValueLoader<T>,
    private val dispatcher: CoroutineDispatcher,
    private val globalScope: CoroutineScope,
    private val cacheTimeoutMillis: Long,
    private val loadingStatusTimeoutMillis: Long
) : LazyFlowLoader<T> {

    private var subscribersCount = 0
    private var scope: CoroutineScope? = null
    private var cancellationJob: Job? = null
    private var inputFlow = MutableStateFlow<Value<T>>(Value.LoadValue(valueLoader))
    private val outputFlow = MutableStateFlow<ResultContainer<T>>(ResultContainer.Loading)

    private val mutex = Mutex()

    override fun listen(): Flow<ResultContainer<T>> = callbackFlow {
        synchronized(this@DefaultLazyFlowLoader) {
            onStart()
        }

        val job = scope?.launch {
            outputFlow.collect {
                trySend(it)
            }
        }

        awaitClose {
            synchronized(this@DefaultLazyFlowLoader) {
                onStop(job)
            }
        }
    }

    override fun update(updater: (ResultContainer<T>) -> ResultContainer<T>) {
        inputFlow.value = Value.InstantValue(resultContainer = updater(outputFlow.value))
    }

    override fun update(container: ResultContainer<T>) {
        inputFlow.value = Value.InstantValue(resultContainer = container)
    }

    private fun prepareNewLoad(
        createdCompletableDeferred: Boolean,
        silently: Boolean,
        valueLoader: ValueLoader<T>?
    ): CompletableDeferred<T>? {
        val oldLoad = inputFlow.value
        if (oldLoad is Value.LoadValue && oldLoad.completableDeferred?.isActive == true) {
            oldLoad.completableDeferred.cancel()
        }
        if (valueLoader != null) {
            this.valueLoader = valueLoader
        }
        val completableDeferred = if (createdCompletableDeferred) {
            CompletableDeferred<T>()
        } else {
            null
        }

        inputFlow.value = Value.LoadValue(this.valueLoader, silently, completableDeferred)
        return completableDeferred
    }

    override fun newAsyncLoad(silently: Boolean, valueLoader: ValueLoader<T>?) {
        prepareNewLoad(
            createdCompletableDeferred = false,
            silently = silently,
            valueLoader = valueLoader
        )
    }

    override suspend fun newLoad(silently: Boolean, valueLoader: ValueLoader<T>?): T =
        mutex.withLock {
            val completableDeferred = prepareNewLoad(
                createdCompletableDeferred = true,
                silently = silently,
                valueLoader = valueLoader
            )
            completableDeferred!!.await()
        }

    private fun onStart() {
        subscribersCount++
        if (subscribersCount == 1) {
            cancellationJob?.cancel()
            startLoading()
        }
    }

    private fun onStop(job: Job?) {
        subscribersCount--
        job?.cancel()
        if (subscribersCount == 0) {
            cancellationJob = globalScope.launch {
                delay(cacheTimeoutMillis)
                synchronized(this@DefaultLazyFlowLoader) {
                    if (subscribersCount == 0) {
                        scope?.cancel()
                        scope = null
                    }
                }
            }
        }
    }

    private fun startLoading() {
        if (scope != null) return
        outputFlow.value = ResultContainer.Loading
        scope = CoroutineScope(SupervisorJob() + dispatcher)
        scope?.launch {
            inputFlow.collectLatest {
                when (it) {
                    is Value.InstantValue -> outputFlow.value = it.resultContainer
                    is Value.LoadValue -> loadValue(it)
                }
            }
        }
    }

    private suspend fun loadValue(loadValue: Value.LoadValue<T>) {
        try {
            val job = scope?.launch {
                delay(loadingStatusTimeoutMillis)
                if (!loadValue.silently) outputFlow.value = ResultContainer.Loading
            }
            val value = loadValue.loader()
            job?.cancel()
            outputFlow.value = ResultContainer.Done(value)
            mutex.withLock {
                loadValue.completableDeferred?.complete(value)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            outputFlow.value = ResultContainer.Error(e)
            mutex.withLock {
                loadValue.completableDeferred?.completeExceptionally(e)
            }
        }
    }

    sealed class Value<T> {
        class InstantValue<T>(val resultContainer: ResultContainer<T>) : Value<T>()
        class LoadValue<T>(
            val loader: ValueLoader<T>,
            val silently: Boolean = false,
            val completableDeferred: CompletableDeferred<T>? = null
        ) : Value<T>()
    }

}
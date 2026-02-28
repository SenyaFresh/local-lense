package ru.hse.locallense.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import ru.hse.locallense.common.Core
import ru.hse.locallense.common.Resources
import ru.hse.locallense.common.Toaster

typealias Action = () -> Unit

/**
 * A base ViewModel class that provides common functionality such as managing a coroutine scope, debouncing actions,
 * and accessing global resources like resources and toasts.
 */
@OptIn(FlowPreview::class)
open class BaseViewModel: ViewModel() {

    /** The coroutine scope associated with this ViewModel. */
    protected val viewModelScope: CoroutineScope by lazy {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            Core.errorHandler.handleError(throwable)
        }
        CoroutineScope(SupervisorJob() + Dispatchers.Main + errorHandler)
    }

    /** Access to global resources (strings, drawables, etc.) through the Core object. */
    protected val resources: Resources get() = Core.resources

    /** A utility to show toast messages globally via Core.toaster. */
    protected val toaster: Toaster get() = Core.toaster

    /** A shared flow used for debouncing actions. */
    private val debounceFlow = MutableSharedFlow<Action>(
        replay = 1,
        extraBufferCapacity = 20
    )

    init {
        viewModelScope.launch {
            debounceFlow.sample(Core.debounceTimeoutMillis).collect {
                it()
            }
        }
    }

    /**
     * Emits a debounced action to avoid executing it repeatedly in a short period.
     *
     * @param action The action to be debounced and emitted.
     */
    protected fun debounce(action: Action) {
        debounceFlow.tryEmit(action)
    }

    /**
     * Cancels the viewModelScope when the ViewModel is cleared, to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
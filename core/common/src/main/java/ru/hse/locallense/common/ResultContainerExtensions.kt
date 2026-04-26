package ru.hse.locallense.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

suspend fun <T> Flow<ResultContainer<T>>.unwrapFirstNotPending(remoteTimeoutMillis: Long = Core.remoteTimeoutMillis): T {
    return withTimeout(remoteTimeoutMillis) {
        filterNot { it is ResultContainer.Loading }
            .first()
            .unwrap()
    }
}
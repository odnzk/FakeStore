package com.example.fakestore.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Store<T>(initialState: T) {

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow: StateFlow<T> = _stateFlow

    private val mutex = Mutex()

    // suspend because of withLock
    suspend fun update(updateBlock: (T) -> T) = mutex.withLock {
        val newState = updateBlock(_stateFlow.value)
        _stateFlow.value = newState
    }

    suspend fun read(readBlock: (T) -> Unit) = mutex.withLock {
        readBlock(_stateFlow.value)
    }
}
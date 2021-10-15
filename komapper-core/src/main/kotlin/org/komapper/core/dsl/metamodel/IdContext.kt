package org.komapper.core.dsl.metamodel

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IdContext(startWith: Int, val incrementBy: Int) {
    private val mutex = Mutex()
    private var base = startWith.toLong()
    private var step = Long.MAX_VALUE

    suspend fun next(nextValue: suspend () -> Long): Long {
        return mutex.withLock {
            if (step < incrementBy) {
                base + step++
            } else {
                nextValue().also {
                    base = it
                    step = 1
                }
            }
        }
    }
}

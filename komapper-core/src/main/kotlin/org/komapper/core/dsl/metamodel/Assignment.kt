package org.komapper.core.dsl.metamodel

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

sealed class Assignment<E> {
    class Identity<E, T : Any>(
        private val klass: KClass<T>,
        private val setter: (E, T) -> E
    ) :
        Assignment<E>() {

        fun assign(entity: E, value: Long): E {
            return setter(entity, value.convert(klass))
        }
    }

    class Sequence<E, T : Any>(
        private val klass: KClass<T>,
        private val setter: (E, T) -> E,
        val name: String,
        val catalogName: String,
        val schemaName: String,
        val alwaysQuote: Boolean,
        val incrementBy: Int,
    ) :
        Assignment<E>() {

        private val contextMap = ConcurrentHashMap<String, GenerationContext>()

        fun assign(entity: E, key: String, enquote: (String) -> String, sequenceNextValue: (String) -> Long): E {
            val context = contextMap.computeIfAbsent(key) {
                val sequenceName = getCanonicalSequenceName(enquote)
                GenerationContext(incrementBy) {
                    sequenceNextValue(sequenceName)
                }
            }
            val value = context.next().convert(klass)
            return setter(entity, value)
        }

        fun getCanonicalSequenceName(enquote: (String) -> String): String {
            val transform = if (alwaysQuote) {
                enquote
            } else {
                { it }
            }
            return listOf(catalogName, schemaName, name)
                .filter { it.isNotBlank() }.joinToString(".", transform = transform)
        }

        private class GenerationContext(private val incrementBy: Int, private val nextValue: () -> Long) {
            private val lock = ReentrantLock()
            private var base = 0L
            private var step = Long.MAX_VALUE

            fun next(): Long {
                return lock.withLock {
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
    }
}

private fun <T : Any> Long.convert(klass: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    return when (klass) {
        Int::class -> this.toInt()
        Long::class -> this
        else -> error("Conversion target class must be either Int or Long.")
    } as T
}

package org.komapper.core.metamodel

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

interface IdGeneratorDescriptor<E, T> {
    fun createAssignment(setter: (Pair<E, T>) -> E): Assignment<E>
}

class IdentityGeneratorDescriptor<E, T : Any>(
    private val klass: KClass<T>
) : IdGeneratorDescriptor<E, T> {

    private fun generate(nextValue: NextValue): T {
        val value = nextValue.get()
        return value.convert(klass)
    }

    override fun createAssignment(setter: (Pair<E, T>) -> E): Assignment<E> {
        return Assignment.Identity(this::generate, setter)
    }
}

class SequenceGeneratorDescriptor<E, T : Any>(
    private val klass: KClass<T>,
    private val name: String,
    private val incrementBy: Int
) : IdGeneratorDescriptor<E, T> {

    private val contexts = ConcurrentHashMap<String, GenerationContext>()

    private fun generate(key: String, nextValue: NextValue): T {
        val context = contexts.computeIfAbsent(key) {
            GenerationContext(incrementBy, nextValue)
        }
        val value = context.next()
        return value.convert(klass)
    }

    override fun createAssignment(setter: (Pair<E, T>) -> E): Assignment<E> {
        return Assignment.Sequence(name, this::generate, setter)
    }

    class GenerationContext(private val incrementBy: Int, private val nextValue: NextValue) {
        private val lock = ReentrantLock()
        private var base = 0L
        private var step = Long.MAX_VALUE

        fun next(): Long {
            return lock.withLock {
                if (step < incrementBy) {
                    base + step++
                } else {
                    nextValue.get().also {
                        base = it
                        step = 1
                    }
                }
            }
        }
    }
}

fun interface NextValue {
    fun get(): Long
}

sealed class Assignment<E> {
    class Identity<E, T>(
        private val generator: (NextValue) -> T,
        private val setter: (Pair<E, T>) -> E
    ) :
        Assignment<E>() {
        fun assign(entity: E, nextValue: NextValue): E {
            val value = generator(nextValue)
            return setter(entity to value)
        }
    }

    class Sequence<E, T>(
        val name: String,
        private val generator: (key: String, NextValue) -> T,
        private val setter: (Pair<E, T>) -> E
    ) :
        Assignment<E>() {
        fun assign(entity: E, key: String, nextValue: NextValue): E {
            val value = generator(key, nextValue)
            return setter(entity to value)
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

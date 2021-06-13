package org.komapper.core.dsl.metamodel

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.komapper.core.ThreadSafe
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@ThreadSafe
sealed class Assignment<ENTITY> {
    class AutoIncrement<ENTITY, EXTERIOR : Any, INTERIOR : Any>(
        val columnName: String,
        private val interiorClass: KClass<INTERIOR>,
        private val wrap: (INTERIOR) -> EXTERIOR,
        private val setter: (ENTITY, EXTERIOR) -> ENTITY,
    ) :
        Assignment<ENTITY>() {

        fun assign(entity: ENTITY, value: Long): ENTITY {
            val interior = value.convert(interiorClass)
            val exterior = wrap(interior)
            return setter(entity, exterior)
        }
    }

    class Sequence<ENTITY, EXTERIOR : Any, INTERIOR : Any>(
        private val interiorClass: KClass<INTERIOR>,
        private val wrap: (INTERIOR) -> EXTERIOR,
        private val setter: (ENTITY, EXTERIOR) -> ENTITY,
        val name: String,
        val catalogName: String,
        val schemaName: String,
        val alwaysQuote: Boolean,
        val startWith: Int,
        val incrementBy: Int,
    ) :
        Assignment<ENTITY>() {

        private val contextMap = ConcurrentHashMap<UUID, GenerationContext>()

        suspend fun assign(
            entity: ENTITY,
            key: UUID,
            enquote: (String) -> String,
            sequenceNextValue: suspend (String) -> Long
        ): ENTITY {
            val context = contextMap.computeIfAbsent(key) {
                val sequenceName = getCanonicalSequenceName(enquote)
                GenerationContext(startWith, incrementBy) {
                    sequenceNextValue(sequenceName)
                }
            }
            val interior = context.next().convert(interiorClass)
            val exterior = wrap(interior)
            return setter(entity, exterior)
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

        private class GenerationContext(startWith: Int, val incrementBy: Int, val nextValue: suspend () -> Long) {
            private val mutex = Mutex()
            private var base = startWith.toLong()
            private var step = Long.MAX_VALUE

            suspend fun next(): Long {
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
    }
}

private fun <T : Any> Long.convert(klass: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    return when (klass) {
        Int::class -> this.toInt()
        Long::class -> this
        UInt::class -> this.toUInt()
        else -> error("Conversion target class must be either Int, UInt or Long.")
    } as T
}

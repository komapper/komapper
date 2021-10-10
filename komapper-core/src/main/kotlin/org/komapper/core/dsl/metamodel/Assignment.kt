package org.komapper.core.dsl.metamodel

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.komapper.core.ThreadSafe
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
sealed class Assignment<ENTITY> {

    class AutoIncrement<ENTITY : Any, ID>(
        private val toId: (Long) -> ID?,
        private val setId: (ENTITY, ID) -> ENTITY,
        val columnName: String
    ) :
        Assignment<ENTITY>() {

        fun assign(entity: ENTITY, generatedKey: Long): ENTITY {
            return executeAssignment(entity, generatedKey, toId, setId)
        }
    }

    class Sequence<ENTITY : Any, ID>(
        private val toId: (Long) -> ID?,
        private val setId: (ENTITY, ID) -> ENTITY,
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
            val generatedKey = context.next()
            return executeAssignment(entity, generatedKey, toId, setId)
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

private fun <ENTITY, ID> executeAssignment(
    entity: ENTITY,
    generatedKey: Long,
    toId: (Long) -> ID?,
    setId: (ENTITY, ID) -> ENTITY
): ENTITY {
    val id = toId(generatedKey)
    checkNotNull(id) { "generatedKey: $generatedKey" }
    return setId(entity, id)
}

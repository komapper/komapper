package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
sealed class IdAssignment<ENTITY> {

    class AutoIncrement<ENTITY : Any, ID : Any>(
        private val toId: (Long) -> ID?,
        val property: PropertyMetamodel<ENTITY, ID, *>,
    ) :
        IdAssignment<ENTITY>() {

        fun assign(entity: ENTITY, generatedKey: Long): ENTITY {
            return executeAssignment(entity, property, generatedKey, toId)
        }
    }

    class Sequence<ENTITY : Any, ID : Any>(
        private val toId: (Long) -> ID?,
        private val property: PropertyMetamodel<ENTITY, ID, *>,
        private val idContextMap: ConcurrentHashMap<UUID, IdContext> = ConcurrentHashMap(),
        val name: String,
        val catalogName: String,
        val schemaName: String,
        val alwaysQuote: Boolean,
        val startWith: Int,
        val incrementBy: Int,
        val disableSequenceAssignment: Boolean
    ) :
        IdAssignment<ENTITY>() {

        suspend fun assign(
            entity: ENTITY,
            key: UUID,
            enquote: (String) -> String,
            sequenceNextValue: suspend (String) -> Long
        ): ENTITY {
            val context = idContextMap.computeIfAbsent(key) {
                IdContext(startWith, incrementBy)
            }
            val generatedKey = context.next {
                val sequenceName = getCanonicalSequenceName(enquote)
                sequenceNextValue(sequenceName)
            }
            return executeAssignment(entity, property, generatedKey, toId)
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
    }
}

private fun <ENTITY : Any, ID : Any> executeAssignment(
    entity: ENTITY,
    property: PropertyMetamodel<ENTITY, ID, *>,
    generatedKey: Long,
    toId: (Long) -> ID?
): ENTITY {
    val id = toId(generatedKey)
    checkNotNull(id) { "generatedKey: $generatedKey" }
    return property.setter(entity, id)
}

package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
sealed class IdGenerator<ENTITY : Any, ID : Any> {

    abstract val property: PropertyMetamodel<ENTITY, ID, *>

    class AutoIncrement<ENTITY : Any, ID : Any>(
        override val property: PropertyMetamodel<ENTITY, ID, *>,
    ) : IdGenerator<ENTITY, ID>()

    class Sequence<ENTITY : Any, ID : Any>(
        override val property: PropertyMetamodel<ENTITY, ID, *>,
        private val toId: (Long) -> ID?,
        private val idContextMap: ConcurrentHashMap<UUID, IdContext>,
        val name: String,
        val catalogName: String,
        val schemaName: String,
        val alwaysQuote: Boolean,
        val startWith: Int,
        val incrementBy: Int,
    ) :
        IdGenerator<ENTITY, ID>() {

        suspend fun generate(
            key: UUID,
            enquote: (String) -> String,
            sequenceNextValue: suspend (String) -> Long
        ): ID {
            val context = idContextMap.computeIfAbsent(key) {
                IdContext(startWith, incrementBy)
            }
            val generatedKey = context.next {
                val sequenceName = getCanonicalSequenceName(enquote)
                sequenceNextValue(sequenceName)
            }
            return toId(generatedKey)!!
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

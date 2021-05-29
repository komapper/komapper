package org.komapper.r2dbc.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityUpdateOption

@ThreadSafe
interface EntityUpdateQueryBuilder<ENTITY : Any> {
    fun option(configure: (EntityUpdateOption) -> EntityUpdateOption): EntityUpdateQueryBuilder<ENTITY>
    fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY>
    fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilder<ENTITY>
    fun single(entity: ENTITY): Query<ENTITY>
}

internal data class EntityUpdateQueryBuilderImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val option: EntityUpdateOption = EntityUpdateOption.default
) :
    EntityUpdateQueryBuilder<ENTITY> {

    override fun option(configure: (EntityUpdateOption) -> EntityUpdateOption): EntityUpdateQueryBuilderImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun include(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilderImpl<ENTITY, ID, META> {
        val newContext = context.copy(includedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    override fun exclude(vararg properties: PropertyMetamodel<ENTITY, *, *>): EntityUpdateQueryBuilderImpl<ENTITY, ID, META> {
        val newContext = context.copy(excludedProperties = properties.toList()).also {
            checkContext(it)
        }
        return copy(context = newContext)
    }

    private fun checkContext(context: EntityUpdateContext<ENTITY, ID, META>) {
        if (context.getTargetProperties().isEmpty()) {
            error(
                "Illegal SQL will be generated. The set clause is empty. " +
                    "Include or exclude appropriate properties."
            )
        }
    }

    override fun single(entity: ENTITY): Query<ENTITY> {
        context.target.checkIdValueNotNull(entity)
        return EntityUpdateSingleQuery(context, option, entity)
    }
}

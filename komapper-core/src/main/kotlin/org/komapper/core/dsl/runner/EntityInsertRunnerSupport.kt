package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator

internal class EntityInsertRunnerSupport<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
) {
    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityInsertStatementBuilder(BuilderDialect(config), context, entities)
        return builder.build()
    }

    internal fun postInsert(entity: ENTITY, generatedKey: Long): ENTITY {
        val idGenerator = context.target.idGenerator()
        return if (idGenerator is IdGenerator.AutoIncrement<ENTITY, ID>) {
            val id = context.target.convertToId(generatedKey)!!
            idGenerator.property.setter(entity, id)
        } else {
            entity
        }
    }
}

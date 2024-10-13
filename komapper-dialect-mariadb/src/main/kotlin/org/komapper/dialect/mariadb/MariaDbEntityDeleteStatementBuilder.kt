package org.komapper.dialect.mariadb

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class MariaDbEntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityDeleteStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = DefaultEntityDeleteStatementBuilder(dialect, context, entity)
    private val support = MariaDbStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class PostgreSqlEntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityDeleteStatementBuilder<ENTITY, ID, META> {

    private val buf = StatementBuffer()
    private val builder = DefaultEntityDeleteStatementBuilder(dialect, context, entity)
    private val support = PostgreSqlStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

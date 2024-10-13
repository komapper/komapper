package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class PostgreSqlRelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = DefaultRelationDeleteStatementBuilder(dialect, context)
    private val support = PostgreSqlStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

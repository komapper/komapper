package org.komapper.dialect.mariadb

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class MariaDbRelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = DefaultRelationDeleteStatementBuilder(dialect, context)
    private val support = MariaDbStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

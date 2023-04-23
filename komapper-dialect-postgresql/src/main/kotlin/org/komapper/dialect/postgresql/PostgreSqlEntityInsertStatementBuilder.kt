package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class PostgreSqlEntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityInsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>,
) : EntityInsertStatementBuilder<ENTITY, ID, META> {

    private val buf = StatementBuffer()
    private val builder = DefaultEntityInsertStatementBuilder(dialect, context, entities)
    private val support = PostgreSqlStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

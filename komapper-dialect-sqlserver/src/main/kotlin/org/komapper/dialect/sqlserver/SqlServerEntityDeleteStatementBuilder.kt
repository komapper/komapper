package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class SqlServerEntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityDeleteStatementBuilder<ENTITY, ID, META> {
    private val builder = DefaultEntityDeleteStatementBuilder(dialect, context, entity)
    private val support = SqlServerStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildDeleteFrom())
        val tablePrefix = SqlServerStatementBuilderSupport.TablePrefix.DELETED
        buf.appendIfNotEmpty(support.buildOutput(tablePrefix))
        buf.appendIfNotEmpty(builder.buildWhere())
        return buf.toStatement()
    }
}

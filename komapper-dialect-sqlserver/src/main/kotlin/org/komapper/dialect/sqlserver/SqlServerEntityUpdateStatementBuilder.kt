package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class SqlServerEntityUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityUpdateContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityUpdateStatementBuilder<ENTITY, ID, META> {

    private val builder = DefaultEntityUpdateStatementBuilder(dialect, context, entity)
    private val support = SqlServerStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildUpdateSet())
        buf.appendIfNotEmpty(support.buildOutput())
        buf.append(" ")
        buf.append(builder.buildWhere())
        return buf.toStatement()
    }
}

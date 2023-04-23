package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityInsertStatementBuilder
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class SqlServerEntityInsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityInsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>,
) : EntityInsertStatementBuilder<ENTITY, ID, META> {

    private val builder = DefaultEntityInsertStatementBuilder(dialect, context, entities)

    private val support = SqlServerBuilderSupport(dialect, context)

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildInsertInto())
        val outputStatement = support.buildOutput()
        if (outputStatement.parts.isNotEmpty()) {
            buf.append(" ")
            buf.append(outputStatement)
        }
        buf.append(" ")
        buf.append(builder.buildValues())
        return buf.toStatement()
    }
}

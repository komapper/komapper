package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityUpdateStatementBuilder
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class OracleEntityUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityUpdateStatementBuilder<ENTITY, ID, META> {

    private val buf = StatementBuffer()
    private val builder = DefaultEntityUpdateStatementBuilder(dialect, context, entity)
    private val support = OracleStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        buf.append(builder.build())
        val returningStatement = support.buildReturning()
        if (returningStatement.parts.isNotEmpty()) {
            buf.append(" ")
            buf.append(returningStatement)
        }
        return buf.toStatement()
    }
}

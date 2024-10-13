package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultEntityDeleteStatementBuilder
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class H2EntityDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
) : EntityDeleteStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = DefaultEntityDeleteStatementBuilder(dialect, context, entity)
    private val support = H2StatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        val tableType = H2StatementBuilderSupport.DeltaTableType.OLD
        buf.append(support.buildReturningFirstFragment(tableType))
        buf.append(builder.build())
        buf.append(support.buildReturningLastFragment())
        return buf.toStatement()
    }
}

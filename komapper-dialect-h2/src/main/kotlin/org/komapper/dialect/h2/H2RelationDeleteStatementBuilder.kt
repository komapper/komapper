package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class H2RelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteStatementBuilder<ENTITY, ID, META> {

    private val buf = StatementBuffer()
    private val builder = DefaultRelationDeleteStatementBuilder(dialect, context)
    private val support = H2StatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        val tableType = H2StatementBuilderSupport.DeltaTableType.OLD
        buf.append(support.buildReturningFirstFragment(tableType))
        buf.append(builder.build())
        buf.append(support.buildReturningLastFragment())
        return buf.toStatement()
    }
}

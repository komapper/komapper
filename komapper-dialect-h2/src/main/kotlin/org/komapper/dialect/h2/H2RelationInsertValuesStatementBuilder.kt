package org.komapper.dialect.h2

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class H2RelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = RelationInsertValuesStatementBuilder(dialect, context)
    private val support = H2StatementBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append(support.buildReturningFirstFragment())
        buf.append(builder.build(assignments))
        buf.append(support.buildReturningLastFragment())
        return buf.toStatement()
    }
}

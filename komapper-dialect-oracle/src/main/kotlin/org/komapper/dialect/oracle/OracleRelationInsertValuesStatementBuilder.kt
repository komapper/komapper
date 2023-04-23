package org.komapper.dialect.oracle

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class OracleRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = RelationInsertValuesStatementBuilder(dialect, context)
    private val support = OracleStatementBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append(builder.build(assignments))
        val returningStatement = support.buildReturning()
        if (returningStatement.parts.isNotEmpty()) {
            buf.append(" ")
            buf.append(returningStatement)
        }
        return buf.toStatement()
    }
}

package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class PostgreSqlRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {
    private val buf = StatementBuffer()
    private val builder = RelationInsertValuesStatementBuilder(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append(builder.build(assignments))
        val outputExpressions = context.returning.expressions()
        if (outputExpressions.isNotEmpty()) {
            buf.append(" returning ")
            for (e in outputExpressions) {
                column(e)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }
}

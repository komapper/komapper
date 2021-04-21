package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class SqlInsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: SqlInsertContext<ENTITY, ID, META>
) {
    private val aliasManager = AliasManagerImpl(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val entityMetamodel = context.target
        buf.append("insert into ")
        buf.append(table(entityMetamodel))
        when (val values = context.values) {
            is Values.Pairs -> {
                buf.append(" (")
                for (column in values.pairs.map { it.first }) {
                    if (column.expression in entityMetamodel.idProperties() &&
                        entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
                    ) {
                        continue
                    }
                    buf.append(column(column.expression))
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(") values (")
                for (parameter in values.pairs.map { it.second }) {
                    if (parameter.expression in entityMetamodel.idProperties() &&
                        entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
                    ) {
                        continue
                    }
                    val value = Value(parameter.value, parameter.expression.klass)
                    buf.bind(value)
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(")")
            }
            is Values.Subquery -> {
                buf.append(" (")
                for (p in context.target.properties()) {
                    buf.append(column(p))
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(") ")
                val subqueryContext = values.context
                val statement = buildSubqueryStatement(subqueryContext)
                buf.append(statement)
            }
        }
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>): String {
        return expression.getCanonicalTableName(dialect::enquote)
    }

    private fun column(expression: PropertyExpression<*>): String {
        return expression.getCanonicalColumnName(dialect::enquote)
    }

    private fun buildSubqueryStatement(subqueryContext: SubqueryContext<*>): Statement {
        return support.buildSubqueryStatement(subqueryContext)
    }
}

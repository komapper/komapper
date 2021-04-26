package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.Value
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class SqlInsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: SqlInsertContext<ENTITY, ID, META>
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        buf.append("insert into ")
        buf.append(table(target))
        when (val values = context.values) {
            is Values.Pairs -> {
                buf.append(" (")
                for (property in values.pairs.map { it.first }) {
                    if (property in target.idProperties() &&
                        property.idAssignment is Assignment.AutoIncrement<*, *>
                    ) {
                        continue
                    }
                    buf.append(column(property))
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append(") values (")
                for ((property, argument) in values.pairs) {
                    if (property in target.idProperties() &&
                        property.idAssignment is Assignment.AutoIncrement<*, *>
                    ) {
                        continue
                    }
                    val value = Value(argument.value, argument.klass)
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

    private fun table(metamodel: EntityMetamodel<*, *, *>): String {
        return metamodel.getCanonicalTableName(dialect::enquote)
    }

    private fun column(expression: ColumnExpression<*>): String {
        return expression.getCanonicalColumnName(dialect::enquote)
    }

    private fun buildSubqueryStatement(subqueryContext: SubqueryContext<*>): Statement {
        return support.buildSubqueryStatement(subqueryContext)
    }
}

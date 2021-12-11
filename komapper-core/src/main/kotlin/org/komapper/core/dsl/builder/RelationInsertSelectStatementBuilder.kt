package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

class RelationInsertSelectStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: Dialect,
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        buf.append("insert into ")
        table(target)
        buf.append(" (")
        for (p in target.getNonAutoIncrementProperties()) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") ")
        subquery(context.select)
        return buf.toStatement()
    }

    private fun table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append(name)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun subquery(expression: SubqueryExpression<*>) {
        val predicate: (ColumnExpression<*, *>) -> Boolean = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.AutoIncrement -> {
                { it != idGenerator.property }
            }
            else -> {
                { true }
            }
        }
        val statement = support.buildSubqueryStatement(expression, predicate)
        buf.append(statement)
    }
}

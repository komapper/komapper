package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.getInsertableProperties

class RelationInsertSelectStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
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
        for (p in target.getInsertableProperties()) {
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
        val insertableProperties = context.target.getInsertableProperties()
        val predicate: (ColumnExpression<*, *>) -> Boolean = { column ->
            // Check if this column corresponds to one of the insertable properties
            insertableProperties.any { property ->
                property.columnName == column.columnName
            }
        }
        val statement = support.buildSubqueryStatement(expression.context, predicate)
        buf.append(statement)
    }
}

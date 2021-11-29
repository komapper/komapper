package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

class RelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val dialect: Dialect,
    val context: RelationInsertValuesContext<ENTITY, ID, META>,
    private val idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>?,
    private val versionAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?,
    private val createdAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?,
    private val updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>?,
) {
    private val aliasManager = DefaultAliasManager(context)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    fun build(): Statement {
        val target = context.target
        buf.append("insert into ")
        table(target)
        buf.append(" (")
        val assignments = getAssignments()
        if (assignments.isNotEmpty()) {
            for ((property, _) in assignments) {
                column(property)
                buf.append(", ")
            }
        }
        buf.cutBack(2)
        buf.append(") values (")
        if (assignments.isNotEmpty()) {
            for ((_, operand) in assignments) {
                operand(operand)
                buf.append(", ")
            }
        }
        buf.cutBack(2)
        buf.append(")")
        return buf.toStatement()
    }

    private fun getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
        val assignments = context.getAssignments()
        val properties = assignments.map { it.first }
        val additionalAssignments = listOfNotNull(
            idAssignment,
            versionAssignment,
            createdAtAssignment,
            updatedAtAssignment
        ).filterNot { it.first in properties }
        return (assignments + additionalAssignments)
            .filter { !it.first.isAutoIncrement() }
    }

    private fun table(metamodel: EntityMetamodel<*, *, *>) {
        val name = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append(name)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }
}

package org.komapper.dialect.mariadb

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.builder.getAssignments
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getNonAutoIncrementProperties

class MariaDbEntityUpsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val aliasManager = EmptyAliasManager
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        if (context.getAssignments().isNotEmpty()) {
            error("The 'EntityUpsertQueryBuilder#set' call is not supported. MariaDB cannot refer to the row to be inserted.")
        }
        val properties = target.getNonAutoIncrementProperties()
        buf.append("insert")
        if (context.duplicateKeyType == DuplicateKeyType.IGNORE) {
            buf.append(" ignore")
        }
        buf.append(" into ")
        table(target)
        buf.append(" (")
        for (p in properties) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (p in properties) {
                buf.bind(p.toValue(entity))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" on duplicate key update ")
            for ((left) in assignments) {
                column(left)
                buf.append(" = values(")
                column(left)
                buf.append("), ")
            }
            buf.cutBack(2)
        }
        val expressions = context.returning.expressions()
        if (expressions.isNotEmpty()) {
            buf.append(" returning ")
            for (e in expressions) {
                column(e)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_ONLY)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }
}

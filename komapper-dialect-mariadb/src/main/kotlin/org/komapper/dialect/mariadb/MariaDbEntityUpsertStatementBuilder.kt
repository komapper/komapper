package org.komapper.dialect.mariadb

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getInsertableProperties

class MariaDbEntityUpsertStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertStatementBuilder<ENTITY> {
    private val target = context.target
    private val aliasManager = UpsertAliasManager(dialect, target, context.excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)
    private val mariaDbSupport = MariaDbStatementBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val properties = target.getInsertableProperties()
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
            for ((left, right) in assignments) {
                column(left)
                buf.append(" = ")
                operand(right)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        buf.appendIfNotEmpty(mariaDbSupport.buildReturning())
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>) {
        support.visitTableExpression(expression, TableNameType.NAME_ONLY)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager(
        dialect: BuilderDialect,
        target: TableExpression<*>,
        excluded: TableExpression<*>,
    ) : AliasManager {
        private val aliasMap: Map<TableExpression<*>, String> = mapOf(
            target to target.getCanonicalTableName(dialect::enquote),
            excluded to excluded.tableName(),
        )

        override val index: Int = 0

        override fun getAlias(expression: TableExpression<*>): String? {
            return aliasMap[expression]
        }
    }
}

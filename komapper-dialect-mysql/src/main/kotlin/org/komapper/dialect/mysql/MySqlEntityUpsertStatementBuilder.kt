package org.komapper.dialect.mysql

import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

class MySqlEntityUpsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: MySqlDialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(dialect, target, excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)

    override fun build(): Statement {
        buf.append("insert")
        if (context.duplicateKeyType == DuplicateKeyType.IGNORE) {
            buf.append(" ignore")
        }
        buf.append(" into ")
        table(target, TableNameType.NAME_ONLY)
        buf.append(" (")
        for (
            p in target.properties().filter {
                it.idAssignment !is Assignment.AutoIncrement<ENTITY, *, *>
            }
        ) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values ")
        for (entity in entities) {
            buf.append("(")
            for (
                p in target.properties().filter {
                    it.idAssignment !is Assignment.AutoIncrement<ENTITY, *, *>
                }
            ) {
                buf.bind(p.toValue(entity))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append("), ")
        }
        buf.cutBack(2)
        buf.append(" as ")
        table(excluded, TableNameType.ALIAS_ONLY)
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" on duplicate key update ")
            for ((left, right) in context.assignmentMap) {
                column(left)
                buf.append(" = ")
                operand(right)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun table(expression: TableExpression<*>, tableNameType: TableNameType) {
        support.visitTableExpression(expression, tableNameType)
    }

    private fun column(expression: ColumnExpression<*, *>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager(
        dialect: MySqlDialect,
        target: TableExpression<*>,
        excluded: TableExpression<*>
    ) : AliasManager {

        private val aliasMap: Map<TableExpression<*>, String> = mapOf(
            target to target.getCanonicalTableName(dialect::enquote),
            excluded to excluded.tableName()
        )

        override val index: Int = 0

        override fun getAlias(expression: TableExpression<*>): String? {
            return aliasMap[expression]
        }
    }
}

package org.komapper.jdbc.mysql

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.builder.AliasManager
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EntityMultiUpsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.TableNameType
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel

class MySqlEntityMultiUpsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: Dialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY>,
    EntityMultiUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(dialect, target, excluded)
    private val buf = StatementBuffer(dialect::formatValue)
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
                it.idAssignment !is Assignment.Identity<ENTITY, *>
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
                    it.idAssignment !is Assignment.Identity<ENTITY, *>
                }
            ) {
                val value = Value(p.getter(entity), p.klass)
                buf.bind(value)
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

    private fun table(expression: EntityExpression<*>, tableNameType: TableNameType) {
        support.visitEntityExpression(expression, tableNameType)
    }

    private fun column(expression: PropertyExpression<*>) {
        val name = expression.getCanonicalColumnName(dialect::enquote)
        buf.append(name)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager(
        dialect: Dialect,
        target: EntityMetamodel<*, *, *>,
        excluded: EntityMetamodel<*, *, *>
    ) : AliasManager {

        private val aliasMap: Map<EntityExpression<*>, String> = mapOf(
            target to target.getCanonicalTableName(dialect::enquote),
            excluded to excluded.tableName()
        )

        override val index: Int = 0

        override fun getAlias(expression: EntityExpression<*>): String? {
            return aliasMap[expression]
        }
    }
}

package org.komapper.dialect.h2

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
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class H2EntityUpsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: H2Dialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(target, excluded)
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, aliasManager, buf)
    private val sourceStatementBuilder = SourceStatementBuilder(dialect, context, entities)

    override fun build(): Statement {
        buf.append("merge into ")
        table(target, TableNameType.NAME_AND_ALIAS)
        buf.append(" using (")
        buf.append(sourceStatementBuilder.build())
        buf.append(") as ")
        table(excluded, TableNameType.ALIAS_ONLY)
        buf.append(" on ")
        val excludedPropertyMap = excluded.properties().associateBy { it.name }
        for (key in context.keys) {
            column(key)
            buf.append(" = ")
            column(excludedPropertyMap[key.name]!!)
            buf.append(" and ")
        }
        buf.cutBack(5)
        buf.append(" when not matched then insert values (")
        for (p in excluded.properties()) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" when matched then update set ")
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
        support.visitColumnExpression(expression)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager(
        val target: TableExpression<*>,
        val excluded: TableExpression<*>
    ) : AliasManager {

        override val index: Int = 0

        override fun getAlias(expression: TableExpression<*>): String {
            return when (expression) {
                target -> "t"
                excluded -> excluded.tableName()
                else -> ""
            }
        }
    }

    private class SourceStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        val dialect: H2Dialect,
        val context: EntityUpsertContext<ENTITY, ID, META>,
        val entities: List<ENTITY>
    ) {

        private val buf = StatementBuffer()

        fun build(): Statement {
            val properties = context.target.properties()
            buf.append("select ")
            for (p in properties) {
                column(p)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(" from values ")
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
            buf.append(" as x (")
            for (p in properties) {
                column(p)
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(")")
            return buf.toStatement()
        }

        private fun column(expression: ColumnExpression<*, *>) {
            val name = expression.getCanonicalColumnName(dialect::enquote)
            buf.append(name)
        }
    }
}

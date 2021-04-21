package org.komapper.jdbc.h2

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
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal class H2EntityMultiUpsertStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: Dialect,
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>
) : EntityUpsertStatementBuilder<ENTITY>,
    EntityMultiUpsertStatementBuilder<ENTITY> {

    private val target = context.target
    private val excluded = context.excluded
    private val aliasManager = UpsertAliasManager(target, excluded)
    private val buf = StatementBuffer(dialect::formatValue)
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
        for ((target, source) in target.idProperties().zip(excluded.idProperties())) {
            column(target)
            buf.append(" = ")
            column(source)
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
            for ((left, right) in context.assignmentOperands) {
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
        support.visitPropertyExpression(expression)
    }

    private fun operand(operand: Operand) {
        support.visitOperand(operand)
    }

    private class UpsertAliasManager<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        val target: META,
        val excluded: META
    ) : AliasManager {

        override val index: Int = 0

        override fun getAlias(expression: EntityExpression<*>): String {
            return when (expression) {
                target -> "t"
                excluded -> excluded.tableName()
                else -> ""
            }
        }
    }

    private class SourceStatementBuilder<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
        val dialect: Dialect,
        val context: EntityUpsertContext<ENTITY, ID, META>,
        val entities: List<ENTITY>
    ) {

        private val buf = StatementBuffer(dialect::formatValue)

        fun build(): Statement {
            val properties = context.target.properties()
            buf.append("select ")
            for (p in properties) {
                buf.append(columnName(p))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(" from values ")
            for (entity in entities) {
                buf.append("(")
                for (p in properties) {
                    buf.bind(Value(p.getter(entity), p.klass))
                    buf.append(", ")
                }
                buf.cutBack(2)
                buf.append("), ")
            }
            buf.cutBack(2)
            buf.append(" as x (")
            for (p in properties) {
                buf.append(columnName(p))
                buf.append(", ")
            }
            buf.cutBack(2)
            buf.append(")")
            return buf.toStatement()
        }

        private fun columnName(expression: PropertyExpression<*>): String {
            return expression.getCanonicalColumnName(dialect::enquote)
        }
    }
}

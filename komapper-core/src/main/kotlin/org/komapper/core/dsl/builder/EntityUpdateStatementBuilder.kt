package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val dialect: BuilderDialect,
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY
) {
    private val buf = StatementBuffer()
    private val support = BuilderSupport(dialect, EmptyAliasManager, buf)

    fun build(): Statement {
        val target = context.target
        val idProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        buf.append("update ")
        table(target)
        buf.append(" set ")
        for (p in context.getTargetProperties()) {
            column(p)
            buf.append(" = ")
            buf.bind(p.toValue(entity))
            if (p == versionProperty) {
                buf.append(" + 1")
            }
            buf.append(", ")
        }
        buf.cutBack(2)
        val criteria = context.getWhereCriteria()
        val versionRequired = versionProperty != null && !context.options.disableOptimisticLock
        if (criteria.isNotEmpty() || idProperties.isNotEmpty() || versionRequired) {
            buf.append(" where ")
            for ((index, criterion) in criteria.withIndex()) {
                criterion(index, criterion)
                buf.append(" and ")
            }
            if (idProperties.isNotEmpty()) {
                for (p in idProperties) {
                    column(p)
                    buf.append(" = ")
                    buf.bind(p.toValue(entity))
                    buf.append(" and ")
                }
                if (!versionRequired) {
                    buf.cutBack(5)
                }
            }
            if (versionRequired) {
                checkNotNull(versionProperty)
                column(versionProperty)
                buf.append(" = ")
                buf.bind(versionProperty.toValue(entity))
            }
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

    private fun criterion(index: Int, c: Criterion) {
        support.visitCriterion(index, c)
    }
}

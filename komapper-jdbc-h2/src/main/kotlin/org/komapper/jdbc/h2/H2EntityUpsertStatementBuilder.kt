package org.komapper.jdbc.h2

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment

class H2EntityUpsertStatementBuilder<ENTITY : Any>(
    private val dialect: Dialect,
    private val context: EntityUpsertContext<ENTITY>,
    private val entity: ENTITY
) : EntityUpsertStatementBuilder<ENTITY> {
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, EmptyAliasManager, buf)

    override fun build(): Statement {
        val idProperties = context.entityMetamodel.idProperties()
        val createdAtProperty = context.entityMetamodel.createdAtProperty()
        val properties = context.entityMetamodel.properties()
        buf.append("merge into ")
        table(context.entityMetamodel)
        buf.append(" using dual on ")
        for (p in idProperties) {
            column(p)
            buf.append(" = ")
            val value = Value(p.getter(entity), p.klass)
            buf.bind(value)
            buf.append(" and ")
        }
        buf.cutBack(5)
        buf.append(" when not matched then insert (")
        for (p in properties) {
            column(p)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(") values (")
        for (p in properties) {
            val value = if (p in idProperties &&
                context.entityMetamodel.idAssignment() is Assignment.Identity<ENTITY, *>
            ) {
                Value(null, p.klass)
            } else {
                Value(p.getter(entity), p.klass)
            }
            buf.bind(value)
            buf.append(", ")
        }
        buf.cutBack(2)
        buf.append(")")
        if (context.duplicateKeyType == DuplicateKeyType.UPDATE) {
            buf.append(" when matched then update set ")
            val updateProperties = context.updateProperties.ifEmpty {
                (properties - idProperties).filter { it != createdAtProperty }
            }
            for (p in updateProperties) {
                column(p)
                buf.append(" = ")
                val value = Value(p.getter(entity), p.klass)
                buf.bind(value)
                buf.append(", ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression)
    }

    private fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }
}

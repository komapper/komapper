package org.komapper.jdbc.h2

import org.komapper.core.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.data.Value
import org.komapper.core.dsl.builder.BuilderSupport
import org.komapper.core.dsl.builder.EmptyAliasManager
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.query.VersionOption

class EntityMergeStatementBuilder<ENTITY : Any>(
    private val dialect: Dialect,
    private val context: EntityMergeContext<ENTITY>,
    private val entity: ENTITY,
    private val option: VersionOption
) {
    private val buf = StatementBuffer(dialect::formatValue)
    private val support = BuilderSupport(dialect, EmptyAliasManager, buf)

    fun build(): Statement {
        val idProperties = context.entityMetamodel.idProperties()
        val versionProperty = context.entityMetamodel.versionProperty()
        val createdAtProperty = context.entityMetamodel.createdAtProperty()
        val properties = context.entityMetamodel.properties()
        buf.append("merge into ")
        table(context.entityMetamodel)
        buf.append(" using dual on ")
        for (p in context.on.ifEmpty { idProperties }) {
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
        buf.append(" when matched")
        if (versionProperty != null && !option.ignoreVersion) {
            buf.append(" and ")
            column(versionProperty)
            buf.append(" = ")
            buf.bind(Value(versionProperty.getter(entity), versionProperty.klass))
        }
        buf.append(" then update set ")
        for (p in (properties - idProperties).filter { it != createdAtProperty }) {
            column(p)
            buf.append(" = ")
            val value = Value(p.getter(entity), p.klass)
            buf.bind(value)
            if (p == versionProperty) {
                buf.append(" + 1 ")
            }
            buf.append(", ")
        }
        buf.cutBack(2)
        return buf.toStatement()
    }

    private fun table(expression: EntityExpression<*>) {
        support.visitEntityExpression(expression)
    }

    private fun column(expression: PropertyExpression<*>) {
        support.visitPropertyExpression(expression)
    }
}

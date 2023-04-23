package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationInsertValuesStatementBuilder
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class SqlServerRelationInsertValuesStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertValuesStatementBuilder<ENTITY, ID, META> {

    private val builder = DefaultRelationInsertValuesStatementBuilder(dialect, context)

    private val support = SqlServerBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildInsertInto(assignments))
        val outputStatement = support.buildOutput()
        if (outputStatement.parts.isNotEmpty()) {
            buf.append(" ")
            buf.append(outputStatement)
        }
        buf.append(" ")
        buf.append(builder.buildValues(assignments))
        return buf.toStatement()
    }
}

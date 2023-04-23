package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationUpdateStatementBuilder
import org.komapper.core.dsl.builder.RelationUpdateStatementBuilder
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class SqlServerRelationUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationUpdateContext<ENTITY, ID, META>,
) : RelationUpdateStatementBuilder<ENTITY, ID, META> {

    private val builder = DefaultRelationUpdateStatementBuilder(dialect, context)

    private val support = SqlServerBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildUpdateSet(assignments))
        val outputStatement = support.buildOutput()
        if (outputStatement.parts.isNotEmpty()) {
            buf.append(" ")
            buf.append(outputStatement)
        }
        buf.append(" ")
        buf.append(builder.buildWhere())
        return buf.toStatement()
    }
}

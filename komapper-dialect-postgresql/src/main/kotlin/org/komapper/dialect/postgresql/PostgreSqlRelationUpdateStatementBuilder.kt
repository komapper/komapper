package org.komapper.dialect.postgresql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.RelationUpdateStatementBuilder
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

class PostgreSqlRelationUpdateStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationUpdateContext<ENTITY, ID, META>,
) : RelationUpdateStatementBuilder<ENTITY, ID, META> {

    private val buf = StatementBuffer()
    private val builder = RelationUpdateStatementBuilder(dialect, context)
    private val support = PostgreSqlStatementBuilderSupport(dialect, context)

    override fun build(assignments: List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>): Statement {
        buf.append(builder.build(assignments))
        buf.appendIfNotEmpty(support.buildReturning())
        return buf.toStatement()
    }
}

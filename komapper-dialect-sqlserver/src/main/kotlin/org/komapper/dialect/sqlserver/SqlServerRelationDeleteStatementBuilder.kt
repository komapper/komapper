package org.komapper.dialect.sqlserver

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.builder.DefaultRelationDeleteStatementBuilder
import org.komapper.core.dsl.builder.RelationDeleteStatementBuilder
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class SqlServerRelationDeleteStatementBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    dialect: BuilderDialect,
    context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteStatementBuilder<ENTITY, ID, META> {

    private val builder = DefaultRelationDeleteStatementBuilder(dialect, context)
    private val support = SqlServerStatementBuilderSupport(dialect, context)

    override fun build(): Statement {
        val buf = StatementBuffer()
        buf.append(builder.buildDeleteFrom())
        val tablePrefix = SqlServerStatementBuilderSupport.TablePrefix.DELETED
        buf.appendIfNotEmpty(support.buildOutput(tablePrefix))
        buf.appendIfNotEmpty(builder.buildWhere())
        return buf.toStatement()
    }
}

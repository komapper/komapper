package org.komapper.dialect.mysql

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel

open class MySqlSchemaStatementBuilder(dialect: BuilderDialect) : AbstractSchemaStatementBuilder(dialect) {
    override fun createSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> = emptyList()
    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> = emptyList()
}

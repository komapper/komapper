package org.komapper.dialect.mariadb

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel

open class MariaDbSchemaStatementBuilder(dialect: MariaDbDialect) : AbstractSchemaStatementBuilder<MariaDbDialect>(dialect) {
    override fun createSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> = emptyList()
    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> = emptyList()
}

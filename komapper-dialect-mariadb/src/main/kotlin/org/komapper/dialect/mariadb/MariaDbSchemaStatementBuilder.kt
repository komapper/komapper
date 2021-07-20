package org.komapper.dialect.mariadb

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel

open class MariaDbSchemaStatementBuilder(dialect: MariaDbDialect) : AbstractSchemaStatementBuilder<MariaDbDialect>(dialect) {
    override fun createSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>) = Unit
}

package org.komapper.jdbc.dsl.query

import org.komapper.core.Table
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor

interface MetadataQuery : Query<List<Table>>

internal class MetadataQueryImpl(
    val catalog: String?,
    val schemaName: String?,
    val tableNamePattern: String?,
    val tableTypes: List<String>
) : MetadataQuery {

    override fun accept(visitor: QueryVisitor): QueryRunner {
        if (visitor is JdbcQueryVisitor) {
            return visitor.visit(this)
        }
        return when (visitor) {
            is JdbcQueryVisitor -> visitor.visit(this)
            else -> error("Use org.komapper.jdbc.Database.runQuery to issue MetadataQuery.")
        }
    }
}

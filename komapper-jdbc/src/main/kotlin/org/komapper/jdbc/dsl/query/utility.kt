package org.komapper.jdbc.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.DryRunResult
import org.komapper.core.dsl.query.Query
import org.komapper.core.toDryRunResult
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor

fun Query<*>.dryRun(config: DatabaseConfig = DryRunDatabaseConfig): DryRunResult {
    val runner = this.accept(JdbcQueryVisitor())
    val statement = runner.dryRun(config)
    val result = statement.toDryRunResult(config.dialect)
    return if (config is DryRunDatabaseConfig) {
        result.copy(
            description = "This data was generated using DryRunDatabaseConfig. " +
                "To get more correct information, specify the actual DatabaseConfig instance."
        )
    } else {
        result
    }
}

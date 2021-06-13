package org.komapper.r2dbc.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.DryRunResult
import org.komapper.core.dsl.query.Query
import org.komapper.core.toDryRunResult
import org.komapper.r2dbc.dsl.visitor.R2dbcQueryVisitor

fun Query<*>.dryRun(config: DatabaseConfig = DryRunDatabaseConfig): DryRunResult {
    val runner = this.accept(R2dbcQueryVisitor())
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

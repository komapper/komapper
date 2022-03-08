package org.komapper.core

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.dryRun

/**
 * Executes a dry run.
 *
 * @param query the query
 * @return the result of dry run
 */
fun Database.dryRunQuery(query: Query<*>): DryRunResult {
    return query.dryRun(this.config)
}

/**
 * Executes a dry run.
 *
 * @param block the block that returns a query
 * @return the result of dry run
 */
fun Database.dryRunQuery(block: () -> Query<*>): DryRunResult {
    val query = block()
    return query.dryRun(this.config)
}

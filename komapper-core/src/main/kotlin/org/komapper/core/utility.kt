package org.komapper.core

/**
 * Returns the result of dry run.
 * @param dialect the dialect of the database
 * @param description the description of the dry run result
 * @return The result of dry run
 */
fun Statement.toDryRunResult(dialect: Dialect, description: String = ""): DryRunResult {
    val sql = this.toSql(dialect::replacePlaceHolder)
    val sqlWithArgs = this.toSqlWithArgs(dialect::formatValue)
    return DryRunResult(sql = sql, sqlWithArgs = sqlWithArgs, args = this.args, description = description)
}

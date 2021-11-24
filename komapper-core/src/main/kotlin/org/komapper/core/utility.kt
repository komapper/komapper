package org.komapper.core

fun Statement.toDryRunResult(dialect: Dialect, description: String = ""): DryRunResult {
    val sql = this.toSql(dialect::replacePlaceHolder)
    val sqlWithArgs = this.toSqlWithArgs(dialect::formatValue)
    return DryRunResult(sql = sql, sqlWithArgs = sqlWithArgs, args = this.args, description = description)
}

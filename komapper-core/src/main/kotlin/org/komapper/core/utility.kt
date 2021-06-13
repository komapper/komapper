package org.komapper.core

fun Statement.toDryRunResult(dialect: Dialect): DryRunResult {
    val sql = this.toSql(dialect::replacePlaceHolder)
    val sqlWithArgs = this.toSqlWithArgs(dialect::formatValue)
    return DryRunResult(sql, sqlWithArgs, this.args)
}

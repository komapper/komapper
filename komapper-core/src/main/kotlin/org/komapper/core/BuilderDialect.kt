package org.komapper.core

interface BuilderDialect : Dialect, DataOperator

private class DefaultBuilderDialect(
    dialect: Dialect,
    private val dataOperator: DataOperator,
) : BuilderDialect, Dialect by dialect, DataOperator by dataOperator

fun BuilderDialect(config: DatabaseConfig): BuilderDialect {
    return BuilderDialect(config.dialect, config.dataOperator)
}

fun BuilderDialect(dialect: Dialect, dataOperator: DataOperator): BuilderDialect {
    return DefaultBuilderDialect(dialect, dataOperator)
}

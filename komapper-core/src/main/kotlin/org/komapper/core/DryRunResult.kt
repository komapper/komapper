package org.komapper.core

data class DryRunResult(
    val sql: String,
    val sqlWithArgs: String,
    val args: List<Value>,
    val description: String = ""
)

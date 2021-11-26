package org.komapper.core

data class DryRunResult(
    val sql: String = "",
    val sqlWithArgs: String = "",
    val args: List<Value> = emptyList(),
    val throwable: Throwable? = null,
    val description: String = "",
)

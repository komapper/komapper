package org.komapper.core

/**
 * The result of dry run.
 *
 * @property sql the SQL string
 * @property sqlWithArgs the SQL string with arguments
 * @property args the arguments
 * @property throwable the [Throwable] instance
 * @property description the description
 */
data class DryRunResult(
    val sql: String = "",
    val sqlWithArgs: String = "",
    val args: List<Value> = emptyList(),
    val throwable: Throwable? = null,
    val description: String = "",
)

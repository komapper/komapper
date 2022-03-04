package org.komapper.core

/**
 * Represents log category.
 */
object LogCategory {
    private const val prefix = "org.komapper."
    const val SQL = "${prefix}Sql"
    const val SQL_WITH_ARGS = "${prefix}SqlWithArgs"
    const val TRANSACTION = "${prefix}Transaction"
    const val OTHER = "${prefix}Other"
}

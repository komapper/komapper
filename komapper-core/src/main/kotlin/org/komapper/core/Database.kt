package org.komapper.core

/**
 * Represents a database.
 */
@ThreadSafe
interface Database {
    /**
     * The database configuration.
     */
    val config: DatabaseConfig
}

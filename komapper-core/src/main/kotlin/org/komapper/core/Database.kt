package org.komapper.core

import org.komapper.core.dsl.query.Query
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML

/**
 * A database.
 *
 * @property config the database configuration
 * @constructor creates a database instance
 */
class Database(val config: DatabaseConfig) {

    /**
     * An interface for transaction scope.
     */
    val transaction get() = config.session.getUserTransaction()

    /**
     * A data type factory.
     */
    val factory = Factory(config)

    /**
     * Execute a query.
     * @param block the Query provider
     */
    fun <T> execute(block: () -> Query<T>): T {
        return block().run(config)
    }

    class Factory(val config: DatabaseConfig) {
        /**
         * Creates Array objects.
         *
         * @param typeName the SQL name of the type the elements of the array map to
         * @param elements the elements that populate the returned object
         */
        fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array = config.session.getConnection().use {
            it.createArrayOf(typeName, elements.toTypedArray())
        }

        /**
         * Creates a Blob object.
         */
        fun createBlob(): Blob = config.session.getConnection().use {
            it.createBlob()
        }

        /**
         * Creates a Clob object.
         */
        fun createClob(): Clob = config.session.getConnection().use {
            it.createClob()
        }

        /**
         * Creates a NClob object.
         */
        fun createNClob(): NClob = config.session.getConnection().use {
            it.createNClob()
        }

        /**
         * Creates a SQLXML object.
         */
        fun createSQLXML(): SQLXML = config.session.getConnection().use {
            it.createSQLXML()
        }
    }
}

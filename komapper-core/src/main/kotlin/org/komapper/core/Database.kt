package org.komapper.core

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.EntityQuery
import org.komapper.core.query.Queryable
import org.komapper.core.query.ScriptQuery
import org.komapper.core.query.SqlQuery
import org.komapper.core.query.scope.WhereDeclaration
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
class Database(val config: DefaultDatabaseConfig) {

    /**
     * A transaction scope initiator.
     */
    val transaction by lazy { config.transactionScopeInitiator }

    /**
     * A data type factory.
     */
    val factory = Factory(config)

    fun <ENTITY> find(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY {
        return findOrNull(metamodel, declaration) ?: error("not found.")
    }

    fun <ENTITY> findOrNull(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY? {
        val queryable = SqlQuery.from(metamodel).where(declaration).limit(1).firstOrNull()
        return executeQueryable(queryable)
    }

    fun <ENTITY> insert(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val queryable = EntityQuery.insert(metamodel, entity)
        return executeQueryable(queryable)
    }

    fun <ENTITY> update(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val queryable = EntityQuery.update(metamodel, entity)
        return executeQueryable(queryable)
    }

    fun <ENTITY> delete(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY) {
        val queryable = EntityQuery.delete(metamodel, entity)
        executeQueryable(queryable)
    }

    fun script(sql: CharSequence) {
        val queryable = ScriptQuery.execute(sql.toString())
        executeQueryable(queryable)
    }

    fun <T> execute(block: () -> Queryable<T>): T {
        return executeQueryable(block())
    }

    fun <T> execute(queryable: Queryable<T>): T {
        return executeQueryable(queryable)
    }

    private fun <T> executeQueryable(queryable: Queryable<T>): T {
        return queryable.run(config)
    }

    class Factory(val config: DefaultDatabaseConfig) {
        /**
         * Creates Array objects.
         *
         * @param typeName the SQL name of the type the elements of the array map to
         * @param elements the elements that populate the returned object
         */
        fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array = config.connection.use {
            it.createArrayOf(typeName, elements.toTypedArray())
        }

        /**
         * Creates a Blob object.
         */
        fun createBlob(): Blob = config.connection.use {
            it.createBlob()
        }

        /**
         * Creates a Clob object.
         */
        fun createClob(): Clob = config.connection.use {
            it.createClob()
        }

        /**
         * Creates a NClob object.
         */
        fun createNClob(): NClob = config.connection.use {
            it.createNClob()
        }

        /**
         * Creates a SQLXML object.
         */
        fun createSQLXML(): SQLXML = config.connection.use {
            it.createSQLXML()
        }
    }
}

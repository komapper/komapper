package org.komapper.core

import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.ScriptQuery
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.EntityMetamodel
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

    fun <ENTITY> find(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY {
        val query = createFindQuery(metamodel, declaration).first()
        return runQuery(query)
    }

    fun <ENTITY> findOrNull(metamodel: EntityMetamodel<ENTITY>, declaration: WhereDeclaration): ENTITY? {
        val query = createFindQuery(metamodel, declaration).firstOrNull()
        return runQuery(query)
    }

    private fun <ENTITY> createFindQuery(
        metamodel: EntityMetamodel<ENTITY>,
        declaration: WhereDeclaration
    ): ListQuery<ENTITY> {
        return SqlQuery.from(metamodel).where(declaration).limit(1)
    }

    fun <ENTITY> insert(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val queryable = EntityQuery.insert(metamodel, entity)
        return runQuery(queryable)
    }

    fun <ENTITY> update(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY): ENTITY {
        val queryable = EntityQuery.update(metamodel, entity)
        return runQuery(queryable)
    }

    fun <ENTITY> delete(metamodel: EntityMetamodel<ENTITY>, entity: ENTITY) {
        val queryable = EntityQuery.delete(metamodel, entity)
        runQuery(queryable)
    }

    fun script(sql: CharSequence) {
        val queryable = ScriptQuery.execute(sql.toString())
        runQuery(queryable)
    }

    fun <T> execute(block: () -> Query<T>): T {
        return runQuery(block())
    }

    fun <T> execute(query: Query<T>): T {
        return runQuery(query)
    }

    private fun <T> runQuery(query: Query<T>): T {
        return query.run(config)
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

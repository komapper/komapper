package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML

/**
 * Represents a factory for JDBC-specific data.
 */
@ThreadSafe
interface JdbcDataFactory {
    fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array
    fun createBlob(): Blob
    fun createClob(): Clob
    fun createNClob(): NClob
    fun createSQLXML(): SQLXML
}

class DefaultJdbcDataFactory(private val session: JdbcSession) : JdbcDataFactory {
    /**
     * Creates Array objects.
     *
     * @param typeName the SQL name of the type the elements of the array map to
     * @param elements the elements that populate the returned object
     */
    override fun createArrayOf(typeName: String, elements: List<*>): java.sql.Array {
        return session.useConnection {
            it.createArrayOf(typeName, elements.toTypedArray())
        }
    }

    /**
     * Creates a Blob object.
     */
    override fun createBlob(): Blob {
        return session.useConnection {
            it.createBlob()
        }
    }

    /**
     * Creates a Clob object.
     */
    override fun createClob(): Clob {
        return session.useConnection {
            it.createClob()
        }
    }

    /**
     * Creates a NClob object.
     */
    override fun createNClob(): NClob {
        return session.useConnection {
            it.createNClob()
        }
    }

    /**
     * Creates a SQLXML object.
     */
    override fun createSQLXML(): SQLXML {
        return session.useConnection {
            it.createSQLXML()
        }
    }
}

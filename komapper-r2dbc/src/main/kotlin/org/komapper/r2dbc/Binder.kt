package org.komapper.r2dbc

import io.r2dbc.spi.Statement
import org.komapper.core.StatementPart
import org.komapper.core.ThreadSafe

/**
 * Binds values to the SQL statement.
 */
@ThreadSafe
interface Binder {
    /**
     * Creates a bind variable name.
     *
     * @param index the binding index
     * @param value the bind variable name
     */
    fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence

    /**
     * Binds the value to the SQL statement.
     *
     * @param statement the SQL statement
     * @param index the index of the value
     * @param value the value
     * @param dataType the data type
     */
    fun <T : Any> bind(statement: Statement, index: Int, value: T?, dataType: R2dbcDataType<T>)
}

object DefaultBinder : Binder {

    override fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence {
        return org.komapper.core.Statement.createBindVariable(index, value)
    }

    override fun <T : Any> bind(statement: Statement, index: Int, value: T?, dataType: R2dbcDataType<T>) {
        dataType.setValue(statement, index, value)
    }
}

object IndexedBinder : Binder {

    override fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence {
        return "$${index + 1}"
    }

    override fun <T : Any> bind(statement: Statement, index: Int, value: T?, dataType: R2dbcDataType<T>) {
        dataType.setValue(statement, "$${index + 1}", value)
    }
}

object AtSignBinder : Binder {

    override fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence {
        return "@p${index + 1}"
    }

    override fun <T : Any> bind(statement: Statement, index: Int, value: T?, dataType: R2dbcDataType<T>) {
        dataType.setValue(statement, "@p${index + 1}", value)
    }
}

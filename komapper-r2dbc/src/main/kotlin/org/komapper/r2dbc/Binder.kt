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
     * Replaces the placeholder.
     *
     * @param index the index of the placeholder
     * @param placeHolder the placeholder
     */
    fun replace(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence

    /**
     * Binds the value to the SQL statement.
     *
     * @param statement the SQL statement
     * @param index the index of the value
     * @param value the value
     * @param dataType the data type
     */
    fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>)
}

object DefaultBinder : Binder {

    override fun replace(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence {
        return placeHolder
    }

    override fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, index, value)
    }
}

object IndexedBinder : Binder {

    override fun replace(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence {
        return "$${index + 1}"
    }

    override fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, "$${index + 1}", value)
    }
}

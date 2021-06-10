package org.komapper.r2dbc

import io.r2dbc.spi.Statement
import org.komapper.core.PlaceHolder
import org.komapper.core.ThreadSafe

@ThreadSafe
interface Binder {
    fun replace(index: Int, placeHolder: PlaceHolder): CharSequence
    fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>)
}

object DefaultBinder : Binder {

    override fun replace(index: Int, placeHolder: PlaceHolder): CharSequence {
        return placeHolder
    }

    override fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, index, value)
    }
}

object IndexedBinder : Binder {

    override fun replace(index: Int, placeHolder: PlaceHolder): CharSequence {
        return "$${index + 1}"
    }

    override fun bind(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, "$${index + 1}", value)
    }
}

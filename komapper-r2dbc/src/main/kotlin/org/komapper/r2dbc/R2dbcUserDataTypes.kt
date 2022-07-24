package org.komapper.r2dbc

import org.komapper.r2dbc.spi.R2dbcUserDataType
import java.util.ServiceLoader

object R2dbcUserDataTypes {

    fun get(): List<R2dbcUserDataType<*>> {
        val loader = ServiceLoader.load(R2dbcUserDataType::class.java)
        return loader.toList()
    }
}

package org.komapper.r2dbc

import org.komapper.core.mustNotBeMarkedNullable
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.util.ServiceLoader

object R2dbcUserDefinedDataTypes {

    fun get(): List<R2dbcUserDefinedDataType<*>> {
        val loader = ServiceLoader.load(R2dbcUserDefinedDataType::class.java)
        return loader.toList().onEach {
            it.type.mustNotBeMarkedNullable(it::class.qualifiedName, "type")
        }
    }
}

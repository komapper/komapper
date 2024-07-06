package org.komapper.jdbc

import org.komapper.core.mustNotBeMarkedNullable
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.util.*

object JdbcUserDefinedDataTypes {

    fun get(): List<JdbcUserDefinedDataType<*>> {
        val loader = ServiceLoader.load(JdbcUserDefinedDataType::class.java)
        return loader.toList().onEach {
            it.type.mustNotBeMarkedNullable(it::class.qualifiedName, "type")
        }
    }
}

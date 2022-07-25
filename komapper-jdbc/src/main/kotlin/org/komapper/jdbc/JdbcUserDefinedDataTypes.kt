package org.komapper.jdbc

import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.util.ServiceLoader

object JdbcUserDefinedDataTypes {

    fun get(): List<JdbcUserDefinedDataType<*>> {
        val loader = ServiceLoader.load(JdbcUserDefinedDataType::class.java)
        return loader.toList()
    }
}

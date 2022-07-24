package org.komapper.jdbc

import org.komapper.jdbc.spi.JdbcUserDataType
import java.util.ServiceLoader

object JdbcUserDataTypes {

    fun get(): List<JdbcUserDataType<*>> {
        val loader = ServiceLoader.load(JdbcUserDataType::class.java)
        return loader.toList()
    }
}

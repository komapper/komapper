package org.komapper.core

import org.komapper.core.spi.DataTypeConverter
import java.util.ServiceLoader

object DataTypeConverters {

    fun get(): List<DataTypeConverter<*, *>> {
        val loader = ServiceLoader.load(DataTypeConverter::class.java)
        return loader.toList()
    }
}

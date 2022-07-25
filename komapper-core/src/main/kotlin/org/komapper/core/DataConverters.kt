package org.komapper.core

import org.komapper.core.spi.DataConverter
import java.util.ServiceLoader

object DataConverters {

    fun get(): List<DataConverter<*, *>> {
        val loader = ServiceLoader.load(DataConverter::class.java)
        return loader.toList()
    }
}

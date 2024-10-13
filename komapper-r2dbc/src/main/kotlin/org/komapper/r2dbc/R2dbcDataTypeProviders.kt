package org.komapper.r2dbc

import org.komapper.core.DataTypeConverters
import org.komapper.core.spi.DataTypeConverter
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.util.ServiceLoader
import kotlin.reflect.KType

object R2dbcDataTypeProviders {
    /**
     * @param driver the driver name
     * @return the [R2dbcDataTypeProvider]
     */
    fun get(driver: String, firstProvider: R2dbcDataTypeProvider? = null): R2dbcDataTypeProvider {
        val loader = ServiceLoader.load(R2dbcDataTypeProviderFactory::class.java)
        val factories = loader.filter { it.supports(driver) }.sortedBy { it.priority }
        val lastProvider: R2dbcDataTypeProvider = R2dbcEmptyDataTypeProvider
        val chainedProviders = factories.fold(lastProvider) { acc, factory -> factory.create(acc) }
        val secondProvider = R2dbcUserDefinedDataTypeProvider
        val converters = DataTypeConverters.get().associateBy { it.exteriorType }
        return object : R2dbcDataTypeProvider {
            override fun <T : Any> get(type: KType): R2dbcDataType<T>? {
                @Suppress("UNCHECKED_CAST")
                val converter = converters[type] as DataTypeConverter<T, Any>?
                return if (converter == null) {
                    find(type)
                } else {
                    val dataType = find<Any>(converter.interiorType)
                        ?: error("The dataType is not found for the type \"${converter.interiorType}\".")
                    R2dbcDataTypeProxy(converter, dataType)
                }
            }

            private fun <T : Any> find(type: KType): R2dbcDataType<T>? {
                return firstProvider?.get(type) ?: secondProvider.get(type) ?: chainedProviders.get(type)
            }
        }
    }
}

private object R2dbcUserDefinedDataTypeProvider : R2dbcDataTypeProvider {
    val dataTypes = R2dbcUserDefinedDataTypes.get().associateBy { it.type }
    override fun <T : Any> get(type: KType): R2dbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypes[type] as R2dbcUserDefinedDataType<T>?
        return if (dataType == null) null else R2dbcUserDefinedDataTypeAdapter(dataType)
    }
}

package org.komapper.jdbc

import org.komapper.core.DataTypeConverters
import org.komapper.core.spi.DataTypeConverter
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.util.ServiceLoader
import kotlin.reflect.KType

object JdbcDataTypeProviders {

    /**
     * @param driver the driver name
     * @return the [JdbcDataTypeProvider]
     */
    fun get(driver: String, firstProvider: JdbcDataTypeProvider? = null): JdbcDataTypeProvider {
        val loader = ServiceLoader.load(JdbcDataTypeProviderFactory::class.java)
        val factories = loader.filter { it.supports(driver) }.sortedBy { it.priority }
        val lastProvider: JdbcDataTypeProvider = EmptyJdbcDataTypeProvider
        val chainedProviders = factories.fold(lastProvider) { acc, factory -> factory.create(acc) }
        val secondProvider = JdbcUserDefinedDataTypeProvider
        val converters = DataTypeConverters.get().associateBy { it.exteriorType }
        return object : JdbcDataTypeProvider {

            override fun <T : Any> get(type: KType): JdbcDataType<T>? {
                @Suppress("UNCHECKED_CAST")
                val converter = converters[type] as DataTypeConverter<T, Any>?
                return if (converter == null) {
                    find(type)
                } else {
                    val dataType: JdbcDataType<Any> = find(converter.interiorType)
                        ?: error("The dataType is not found for the type \"${converter.interiorType}\".")
                    JdbcDataTypeProxy(converter, dataType)
                }
            }

            private fun <T : Any> find(type: KType): JdbcDataType<T>? {
                return firstProvider?.get(type) ?: secondProvider.get(type) ?: chainedProviders.get(type)
            }
        }
    }
}

private object JdbcUserDefinedDataTypeProvider : JdbcDataTypeProvider {
    val dataTypes = JdbcUserDefinedDataTypes.get().associateBy { it.type }
    override fun <T : Any> get(type: KType): JdbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypes[type] as JdbcUserDefinedDataType<T>?
        return if (dataType == null) null else JdbcUserDefinedDataTypeAdapter(dataType)
    }
}

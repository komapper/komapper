package org.komapper.r2dbc

import org.komapper.core.DataTypeConverters
import org.komapper.core.spi.DataTypeConverter
import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import java.util.ServiceLoader
import kotlin.reflect.KClass

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
        val converters = DataTypeConverters.get().associateBy { it.exteriorClass }
        return object : R2dbcDataTypeProvider {

            override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? {
                @Suppress("UNCHECKED_CAST")
                val converter = converters[klass] as DataTypeConverter<T, Any>?
                return if (converter == null) {
                    find(klass)
                } else {
                    val dataType = find(converter.interiorClass)
                        ?: error("The dataType is not found for the type \"${converter.interiorClass.qualifiedName}\".")
                    R2dbcDataTypeProxy(converter, dataType)
                }
            }

            private fun <T : Any> find(klass: KClass<out T>): R2dbcDataType<T>? {
                return firstProvider?.get(klass) ?: secondProvider.get(klass) ?: chainedProviders.get(klass)
            }
        }
    }
}

private object R2dbcUserDefinedDataTypeProvider : R2dbcDataTypeProvider {
    val dataTypes = R2dbcUserDefinedDataTypes.get().associateBy { it.klass }
    override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypes[klass] as R2dbcUserDefinedDataType<T>?
        return if (dataType == null) null else R2dbcUserDefinedDataTypeAdapter(dataType)
    }
}

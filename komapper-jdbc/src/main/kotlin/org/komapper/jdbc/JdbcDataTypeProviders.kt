package org.komapper.jdbc

import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import java.util.ServiceLoader
import kotlin.reflect.KClass

object JdbcDataTypeProviders {

    /**
     * @param driver the driver name
     * @return the [JdbcDataTypeProvider]
     */
    fun get(driver: String, firstProvider: JdbcDataTypeProvider? = null): JdbcDataTypeProvider {
        val secondProvider = JdbcUserDataTypeProvider
        val loader = ServiceLoader.load(JdbcDataTypeProviderFactory::class.java)
        val factories = loader.filter { it.supports(driver) }.sortedBy { it.priority }
        val lastProvider: JdbcDataTypeProvider = EmptyJdbcDataTypeProvider
        val chainedProviders = factories.fold(lastProvider) { acc, factory -> factory.create(acc) }
        return object : JdbcDataTypeProvider {
            override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? {
                return firstProvider?.get(klass) ?: secondProvider.get(klass) ?: chainedProviders.get(klass)
            }
        }
    }
}

private object JdbcUserDataTypeProvider : JdbcDataTypeProvider {
    val dataTypes = JdbcUserDefinedDataTypes.get().associateBy { it.klass }
    override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypes[klass] as JdbcUserDefinedDataType<T>?
        return if (dataType == null) null else JdbcUserDataTypeAdapter(dataType)
    }
}

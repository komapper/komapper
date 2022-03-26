package org.komapper.jdbc

import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory
import java.util.ServiceLoader
import kotlin.reflect.KClass

object JdbcDataTypeProviders {

    /**
     * @param driver the driver name
     * @return the [JdbcDataTypeProvider]
     */
    fun get(driver: String, firstProvider: JdbcDataTypeProvider? = null): JdbcDataTypeProvider {
        val loader = ServiceLoader.load(JdbcDataTypeProviderFactory::class.java)
        val factories = loader.filter { it.supports(driver) }.sortedBy { it.priority }
        val lastProvider: JdbcDataTypeProvider = EmptyJdbcDataTypeProvider
        val nextProvider = factories.fold(lastProvider) { acc, factory -> factory.create(acc) }
        return object : JdbcDataTypeProvider {
            override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? {
                return firstProvider?.get(klass) ?: nextProvider.get(klass)
            }
        }
    }
}

package org.komapper.r2dbc

import org.komapper.r2dbc.spi.R2dbcDataTypeProviderFactory
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
        val nextProvider = factories.fold(lastProvider) { acc, factory -> factory.create(acc) }
        return object : R2dbcDataTypeProvider {
            override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? {
                return firstProvider?.get(klass) ?: nextProvider.get(klass)
            }
        }
    }
}

object R2dbcEmptyDataTypeProvider : R2dbcDataTypeProvider {
    override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? = null
}

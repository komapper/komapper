package org.komapper.jdbc

import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory
import org.komapper.jdbc.spi.JdbcUserDataType
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
    val userDataTypes = JdbcUserDataTypes.get().associateBy { it.klass }
    override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val userDataType = userDataTypes[klass] as JdbcUserDataType<T>?
        return if (userDataType == null) null else JdbcUserDataTypeAdapter(userDataType)
    }
}

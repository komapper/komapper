package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import kotlin.reflect.KClass

@ThreadSafe
interface JdbcDataTypeProvider {
    fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>?
}

abstract class AbstractJdbcDataTypeProvider(
    private val next: JdbcDataTypeProvider,
    dataTypes: List<JdbcDataType<*>>,
) : JdbcDataTypeProvider {

    private val dataTypeMap: Map<KClass<*>, JdbcDataType<*>> = dataTypes.associateBy { it.klass }

    override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypeMap[klass] as JdbcDataType<T>?
        return dataType ?: next.get(klass)
    }
}

class DefaultJdbcDataTypeProvider(
    next: JdbcDataTypeProvider,
    dataTypes: List<JdbcDataType<*>>,
) : AbstractJdbcDataTypeProvider(next, dataTypes)

fun JdbcDataTypeProvider(vararg dataTypes: JdbcDataType<*>): JdbcDataTypeProvider {
    return DefaultJdbcDataTypeProvider(EmptyJdbcDataTypeProvider, dataTypes.toList())
}

internal object EmptyJdbcDataTypeProvider : JdbcDataTypeProvider {
    override fun <T : Any> get(klass: KClass<out T>): JdbcDataType<T>? = null
}

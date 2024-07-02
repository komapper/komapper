package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import kotlin.reflect.KType

@ThreadSafe
interface JdbcDataTypeProvider {
    fun <T : Any> get(type: KType): JdbcDataType<T>?
}

abstract class AbstractJdbcDataTypeProvider(
    private val next: JdbcDataTypeProvider,
    dataTypes: List<JdbcDataType<*>>,
) : JdbcDataTypeProvider {

    private val dataTypeMap: Map<KType, JdbcDataType<*>> = dataTypes.associateBy { it.type }

    override fun <T : Any> get(type: KType): JdbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypeMap[type] as JdbcDataType<T>?
        return dataType ?: next.get(type)
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
    override fun <T : Any> get(type: KType): JdbcDataType<T>? = null
}

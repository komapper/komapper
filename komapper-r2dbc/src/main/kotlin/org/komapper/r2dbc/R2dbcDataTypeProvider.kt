package org.komapper.r2dbc

import org.komapper.core.ThreadSafe
import kotlin.reflect.KType

@ThreadSafe
interface R2dbcDataTypeProvider {
    fun <T : Any> get(type: KType): R2dbcDataType<T>?
}

abstract class AbstractR2dbcDataTypeProvider(
    private val next: R2dbcDataTypeProvider,
    dataTypes: List<R2dbcDataType<*>>,
) : R2dbcDataTypeProvider {
    private val dataTypeMap: Map<KType, R2dbcDataType<*>> = dataTypes.associateBy { it.type }

    override fun <T : Any> get(type: KType): R2dbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypeMap[type] as R2dbcDataType<T>?
        return dataType ?: next.get(type)
    }
}

class DefaultR2dbcDataTypeProvider(
    next: R2dbcDataTypeProvider,
    dataTypes: List<R2dbcDataType<*>>,
) : AbstractR2dbcDataTypeProvider(next, dataTypes)

fun R2dbcDataTypeProvider(vararg dataTypes: R2dbcDataType<*>): R2dbcDataTypeProvider {
    return DefaultR2dbcDataTypeProvider(R2dbcEmptyDataTypeProvider, dataTypes.toList())
}

internal object R2dbcEmptyDataTypeProvider : R2dbcDataTypeProvider {
    override fun <T : Any> get(type: KType): R2dbcDataType<T>? = null
}

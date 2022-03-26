package org.komapper.r2dbc

import org.komapper.core.ThreadSafe
import kotlin.reflect.KClass

@ThreadSafe
interface R2dbcDataTypeProvider {
    fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>?
}

abstract class R2dbcAbstractDataTypeProvider(
    private val next: R2dbcDataTypeProvider,
    dataTypes: List<R2dbcDataType<*>>
) : R2dbcDataTypeProvider {

    private val dataTypeMap: Map<KClass<*>, R2dbcDataType<*>> = dataTypes.associateBy { it.klass }

    override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypeMap[klass] as R2dbcDataType<T>?
        return dataType ?: next.get(klass)
    }
}

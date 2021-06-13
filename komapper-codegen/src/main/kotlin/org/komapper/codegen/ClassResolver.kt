package org.komapper.codegen

import org.komapper.core.Column
import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcDataType
import kotlin.reflect.KClass

@ThreadSafe
fun interface ClassResolver {
    fun resolve(column: Column): KClass<*>?

    companion object {
        fun create(dataTypes: List<JdbcDataType<*>>): ClassResolver {
            return DefaultClassResolver(dataTypes)
        }
    }
}

internal class DefaultClassResolver(private val dataTypes: List<JdbcDataType<*>>) : ClassResolver {

    override fun resolve(column: Column): KClass<*>? {
        val dataType = dataTypes.find {
            it.name.lowercase() == column.typeName.lowercase()
        } ?: dataTypes.find {
            it.jdbcType.vendorTypeNumber == column.dataType
        }
        return dataType?.klass
    }
}

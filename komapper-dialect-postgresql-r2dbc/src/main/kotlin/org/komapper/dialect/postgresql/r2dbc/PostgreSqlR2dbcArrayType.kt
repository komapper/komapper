package org.komapper.dialect.postgresql.r2dbc

import org.komapper.r2dbc.AbstractR2dbcDataType
import org.komapper.r2dbc.R2dbcDataType
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

class PostgreSqlR2dbcArrayType(arrayKlass: KClass<*>, componentDataType: R2dbcDataType<*>) :
    AbstractR2dbcDataType<Array<*>>(typeOf<Array<*>>(), arrayKlass.java) {
    override val name: String = "${componentDataType.name}[]"

    override fun convertBeforeGetting(value: Any): Array<*> {
        return when (value) {
            is Array<*> -> value
            else -> error("Cannot convert. value=$value, type=${value::class.qualifiedName}.")
        }
    }
}

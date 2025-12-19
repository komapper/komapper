package org.komapper.datetime.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.komapper.r2dbc.AbstractR2dbcDataType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class R2dbcDatetimeTypeProvider(val next: R2dbcDataTypeProvider) : R2dbcDataTypeProvider {
    override fun <T : Any> get(type: KType): R2dbcDataType<T>? {
        val dataType: R2dbcDataType<*>? = when (type.classifier as KClass<*>) {
            LocalDate::class -> {
                val dataType = next.get<java.time.LocalDate>(typeOf<java.time.LocalDate>())
                    ?: error("The dataType is not found for java.time.LocalDate.")
                JdbcKotlinLocalDateType(dataType)
            }

            LocalDateTime::class -> {
                val dataType = next.get<java.time.LocalDateTime>(typeOf<java.time.LocalDateTime>())
                    ?: error("The dataType is not found for java.time.LocalDateTime.")
                JdbcKotlinLocalDateTimeType(dataType)
            }

            else -> {
                next.get<T>(type)
            }
        }
        @Suppress("UNCHECKED_CAST")
        return dataType as R2dbcDataType<T>?
    }
}

internal class JdbcKotlinLocalDateType(private val dataType: R2dbcDataType<java.time.LocalDate>) :
    AbstractR2dbcDataType<LocalDate>(typeOf<LocalDate>()) {
    override val name: String = dataType.name

    override fun getValue(row: Row, index: Int): LocalDate? {
        val value = dataType.getValue(row, index)
        return value?.toKotlinLocalDate()
    }

    override fun getValue(row: Row, columnLabel: String): LocalDate? {
        val value = dataType.getValue(row, columnLabel)
        return value?.toKotlinLocalDate()
    }

    override fun setValue(statement: Statement, index: Int, value: LocalDate?) {
        dataType.setValue(statement, index, value?.toJavaLocalDate())
    }

    override fun setValue(statement: Statement, name: String, value: LocalDate?) {
        dataType.setValue(statement, name, value?.toJavaLocalDate())
    }
}

internal class JdbcKotlinLocalDateTimeType(private val dataType: R2dbcDataType<java.time.LocalDateTime>) :
    AbstractR2dbcDataType<LocalDateTime>(typeOf<LocalDateTime>()) {
    override val name: String = dataType.name

    override fun getValue(row: Row, index: Int): LocalDateTime? {
        val value = dataType.getValue(row, index)
        return value?.toKotlinLocalDateTime()
    }

    override fun getValue(row: Row, columnLabel: String): LocalDateTime? {
        val value = dataType.getValue(row, columnLabel)
        return value?.toKotlinLocalDateTime()
    }

    override fun setValue(statement: Statement, index: Int, value: LocalDateTime?) {
        dataType.setValue(statement, index, value?.toJavaLocalDateTime())
    }

    override fun setValue(statement: Statement, name: String, value: LocalDateTime?) {
        dataType.setValue(statement, name, value?.toJavaLocalDateTime())
    }
}

package org.komapper.datetime.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.komapper.r2dbc.AbstractR2dbcDataType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import kotlin.reflect.KClass

class R2dbcDatetimeTypeProvider(val next: R2dbcDataTypeProvider) : R2dbcDataTypeProvider {

    override fun <T : Any> get(klass: KClass<out T>): R2dbcDataType<T>? {
        val dataType: R2dbcDataType<*>? = when (klass) {
            Instant::class -> {
                val dataType = next.get(java.time.Instant::class)
                    ?: error("The dataType is not found for java.time.Instant.")
                JdbcKotlinInstantType(dataType)
            }
            LocalDate::class -> {
                val dataType = next.get(java.time.LocalDate::class)
                    ?: error("The dataType is not found for java.time.LocalDate.")
                JdbcKotlinLocalDateType(dataType)
            }
            LocalDateTime::class -> {
                val dataType = next.get(java.time.LocalDateTime::class)
                    ?: error("The dataType is not found for java.time.LocalDateTime.")
                JdbcKotlinLocalDateTimeType(dataType)
            }
            else -> next.get(klass)
        }
        @Suppress("UNCHECKED_CAST")
        return dataType as R2dbcDataType<T>?
    }
}

internal class JdbcKotlinInstantType(private val dataType: R2dbcDataType<java.time.Instant>) :
    AbstractR2dbcDataType<Instant>(Instant::class) {
    override val name: String = dataType.name

    override fun getValue(row: Row, index: Int): Instant? {
        val value = dataType.getValue(row, index)
        return value?.toKotlinInstant()
    }

    override fun getValue(row: Row, columnLabel: String): Instant? {
        val value = dataType.getValue(row, columnLabel)
        return value?.toKotlinInstant()
    }

    override fun setValue(statement: Statement, index: Int, value: Instant?) {
        dataType.setValue(statement, index, value?.toJavaInstant())
    }

    override fun setValue(statement: Statement, name: String, value: Instant?) {
        dataType.setValue(statement, name, value?.toJavaInstant())
    }
}

internal class JdbcKotlinLocalDateType(private val dataType: R2dbcDataType<java.time.LocalDate>) :
    AbstractR2dbcDataType<LocalDate>(LocalDate::class) {
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
    AbstractR2dbcDataType<LocalDateTime>(LocalDateTime::class) {
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

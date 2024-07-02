package org.komapper.datetime.jdbc

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.komapper.jdbc.AbstractJdbcDataType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class JdbcDatetimeTypeProvider(val next: JdbcDataTypeProvider) : JdbcDataTypeProvider {

    override fun <T : Any> get(type: KType): JdbcDataType<T>? {
        val dataType: JdbcDataType<*>? = when (type.classifier) {
            Instant::class -> {
                val dataType = next.get<java.time.Instant>(typeOf<java.time.Instant>())
                    ?: error("The dataType is not found for java.time.Instant.")
                JdbcKotlinInstantType(dataType)
            }
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
            else -> next.get<T>(type)
        }
        @Suppress("UNCHECKED_CAST")
        return dataType as JdbcDataType<T>?
    }
}

internal class JdbcKotlinInstantType(private val dataType: JdbcDataType<java.time.Instant>) :
    AbstractJdbcDataType<Instant>(typeOf<Instant>(), dataType.jdbcType) {
    override val name: String = dataType.name

    override fun doGetValue(rs: ResultSet, index: Int): Instant? {
        val value = dataType.getValue(rs, index)
        return value?.toKotlinInstant()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): Instant? {
        val value = dataType.getValue(rs, columnLabel)
        return value?.toKotlinInstant()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: Instant) {
        dataType.setValue(ps, index, value.toJavaInstant())
    }
}

internal class JdbcKotlinLocalDateType(private val dataType: JdbcDataType<java.time.LocalDate>) :
    AbstractJdbcDataType<LocalDate>(typeOf<LocalDate>(), dataType.jdbcType) {
    override val name: String = dataType.name

    override fun doGetValue(rs: ResultSet, index: Int): LocalDate? {
        val value = dataType.getValue(rs, index)
        return value?.toKotlinLocalDate()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): LocalDate? {
        val value = dataType.getValue(rs, columnLabel)
        return value?.toKotlinLocalDate()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: LocalDate) {
        dataType.setValue(ps, index, value.toJavaLocalDate())
    }
}

internal class JdbcKotlinLocalDateTimeType(private val dataType: JdbcDataType<java.time.LocalDateTime>) :
    AbstractJdbcDataType<LocalDateTime>(typeOf<LocalDateTime>(), dataType.jdbcType) {
    override val name: String = dataType.name

    override fun doGetValue(rs: ResultSet, index: Int): LocalDateTime? {
        val value = dataType.getValue(rs, index)
        return value?.toKotlinLocalDateTime()
    }

    override fun doGetValue(rs: ResultSet, columnLabel: String): LocalDateTime? {
        val value = dataType.getValue(rs, columnLabel)
        return value?.toKotlinLocalDateTime()
    }

    override fun doSetValue(ps: PreparedStatement, index: Int, value: LocalDateTime) {
        dataType.setValue(ps, index, value.toJavaLocalDateTime())
    }
}

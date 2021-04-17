package org.komapper.jdbc.mysql

import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.jdbc.ArrayType
import org.komapper.core.jdbc.BigDecimalType
import org.komapper.core.jdbc.BigIntegerType
import org.komapper.core.jdbc.BlobType
import org.komapper.core.jdbc.BooleanType
import org.komapper.core.jdbc.ByteArrayType
import org.komapper.core.jdbc.ByteType
import org.komapper.core.jdbc.ClobType
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.DoubleType
import org.komapper.core.jdbc.FloatType
import org.komapper.core.jdbc.IntType
import org.komapper.core.jdbc.LocalDateTimeType
import org.komapper.core.jdbc.LocalDateType
import org.komapper.core.jdbc.LocalTimeType
import org.komapper.core.jdbc.LongType
import org.komapper.core.jdbc.NClobType
import org.komapper.core.jdbc.OffsetDateTimeType
import org.komapper.core.jdbc.SQLXMLType
import org.komapper.core.jdbc.ShortType
import org.komapper.core.jdbc.StringType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLException
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

open class MySqlDialect(val version: Version = Version.V8_0) : AbstractDialect() {

    companion object {
        enum class Version { V8_0 }

        /** the error code that represents unique violation  */
        var UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = setOf(1022, 1062)
    }

    // TODO
    override val openQuote: String
        get() = ""

    // TODO
    override val closeQuote: String
        get() = ""

    override val escapeString: String = "\\\\"

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode in UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getOffsetLimitSql(offset: Int, limit: Int): String {
        if (offset < 0 && limit < 0) {
            return ""
        }
        val buf = StringBuilder(50)
        buf.append(" limit ")
        if (offset >= 0) {
            buf.append(offset)
            buf.append(", ")
        }
        if (limit > 0) {
            buf.append(limit)
        } else {
            buf.append(Long.MAX_VALUE)
        }
        return buf.toString()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDataType(type: KClass<*>): Pair<DataType<*>, String> = when (type) {
        java.sql.Array::class -> ArrayType to "varbinary(500)"
        BigDecimal::class -> BigDecimalType to "decimal"
        BigInteger::class -> BigIntegerType to "decimal"
        Blob::class -> BlobType to "blob"
        Boolean::class -> BooleanType to "bit(1)"
        Byte::class -> ByteType to "tinyint"
        ByteArray::class -> ByteArrayType to "bytea"
        Double::class -> DoubleType to "double precision"
        Clob::class -> ClobType to "text"
        Float::class -> FloatType to "real"
        Int::class -> IntType to "integer"
        LocalDateTime::class -> LocalDateTimeType to "timestamp(6)"
        LocalDate::class -> LocalDateType to "date"
        LocalTime::class -> LocalTimeType to "time"
        Long::class -> LongType to "bigint"
        NClob::class -> NClobType to "text"
        OffsetDateTime::class -> OffsetDateTimeType to "timestamp"
        Short::class -> ShortType to "smallint"
        String::class -> StringType to "varchar(500)"
        SQLXML::class -> SQLXMLType to "text"
        else -> error(
            "The dataType is not found for the type \"${type.qualifiedName}\"."
        )
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return MySqlSchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY> {
        return MySqlEntityUpsertStatementBuilder(this, context, entity)
    }
}

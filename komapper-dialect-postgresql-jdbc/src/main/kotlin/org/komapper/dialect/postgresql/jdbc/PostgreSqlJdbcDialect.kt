package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.AbstractJdbcDialect
import org.komapper.jdbc.ArrayType
import org.komapper.jdbc.BigDecimalType
import org.komapper.jdbc.BigIntegerType
import org.komapper.jdbc.BooleanType
import org.komapper.jdbc.ByteArrayType
import org.komapper.jdbc.ByteType
import org.komapper.jdbc.DoubleType
import org.komapper.jdbc.FloatType
import org.komapper.jdbc.IntType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.LocalDateTimeType
import org.komapper.jdbc.LocalDateType
import org.komapper.jdbc.LocalTimeType
import org.komapper.jdbc.LongType
import org.komapper.jdbc.OffsetDateTimeType
import org.komapper.jdbc.SQLXMLType
import org.komapper.jdbc.ShortType
import org.komapper.jdbc.StringType
import org.komapper.jdbc.UByteType
import org.komapper.jdbc.UIntType
import org.komapper.jdbc.UShortType
import java.sql.SQLException

open class PostgreSqlJdbcDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : PostgreSqlDialect, AbstractJdbcDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"

        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            ArrayType("text[]"),
            BigDecimalType("numeric"),
            BigIntegerType("numeric"),
            BooleanType("boolean"),
            ByteType("smallint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            FloatType("real"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            SQLXMLType("xml"),
            StringType("text"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
            PostgreSqlUUIDType
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }
}

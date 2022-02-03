package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcArrayType
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBooleanType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.SQLException

open class JdbcPostgreSqlDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : PostgreSqlDialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the state code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE = "23505"

        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcArrayType("text[]"),
            JdbcBigDecimalType("numeric"),
            JdbcBigIntegerType("numeric"),
            JdbcBooleanType("boolean"),
            JdbcByteType("smallint"),
            JdbcByteArrayType("bytea"),
            JdbcDoubleType("double precision"),
            JdbcFloatType("real"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("smallint"),
            JdbcSQLXMLType("xml"),
            JdbcStringType("text"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
            JdbcPostgreSqlUUIDType
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
        }
    }
}

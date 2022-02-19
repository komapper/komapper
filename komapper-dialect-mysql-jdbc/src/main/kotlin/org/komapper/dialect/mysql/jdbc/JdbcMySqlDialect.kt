package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobType
import org.komapper.jdbc.JdbcBooleanType
import org.komapper.jdbc.JdbcByteArrayType
import org.komapper.jdbc.JdbcByteType
import org.komapper.jdbc.JdbcClobType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDoubleType
import org.komapper.jdbc.JdbcFloatType
import org.komapper.jdbc.JdbcIntType
import org.komapper.jdbc.JdbcLocalDateTimeType
import org.komapper.jdbc.JdbcLocalDateType
import org.komapper.jdbc.JdbcLocalTimeType
import org.komapper.jdbc.JdbcLongType
import org.komapper.jdbc.JdbcNClobType
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.SQLException

open class JdbcMySqlDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList()
) : MySqlDialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("blob"),
            JdbcBooleanType("bit(1)"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("varbinary(500)"),
            JdbcDoubleType("double precision"),
            JdbcClobType("text"),
            JdbcFloatType("real"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp(6)"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcNClobType("text"),
            JdbcOffsetDateTimeType("timestamp"),
            JdbcShortType("smallint"),
            JdbcStringType("varchar(500)"),
            JdbcSQLXMLType("text"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
        )
    }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

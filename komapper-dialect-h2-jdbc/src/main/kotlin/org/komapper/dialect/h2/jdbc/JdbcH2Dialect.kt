package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcAnyType
import org.komapper.jdbc.JdbcArrayType
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

open class JdbcH2Dialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : H2Dialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505

        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcAnyType("other"),
            JdbcArrayType("array"),
            JdbcBigDecimalType("bigint"),
            JdbcBigIntegerType("bigint"),
            JdbcBlobType("blob"),
            JdbcBooleanType("bool"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("binary"),
            JdbcDoubleType("double"),
            JdbcClobType("clob"),
            JdbcFloatType("float"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcNClobType("nclob"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("smallint"),
            JdbcStringType("varchar(500)"),
            JdbcSQLXMLType("clob"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("integer"),
            JdbcH2UUIDType
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }
}

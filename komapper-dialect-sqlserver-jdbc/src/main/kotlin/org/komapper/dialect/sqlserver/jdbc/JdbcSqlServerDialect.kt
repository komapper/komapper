package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcBigDecimalType
import org.komapper.jdbc.JdbcBigIntegerType
import org.komapper.jdbc.JdbcBlobType
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
import org.komapper.jdbc.JdbcSQLXMLType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.SQLException

class JdbcSqlServerDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
) : SqlServerDialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("varbinary(max)"),
            JdbcByteType("tinyint"),
            JdbcByteArrayType("varbinary(1000)"),
            JdbcClobType("text"),
            JdbcDoubleType("float"),
            JdbcFloatType("real"),
            JdbcIntType("int"),
            JdbcLocalDateTimeType("datetime"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("time"),
            JdbcLongType("bigint"),
            JdbcShortType("smallint"),
            JdbcSQLXMLType("xml"),
            JdbcStringType("varchar(1000)"),
            JdbcUByteType("smallint"),
            JdbcUIntType("bigint"),
            JdbcUShortType("int"),
            JdbcSqlServerBooleanType,
        )
    }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun isSequenceExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
        }
    }

    override fun isTableExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
        }
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}

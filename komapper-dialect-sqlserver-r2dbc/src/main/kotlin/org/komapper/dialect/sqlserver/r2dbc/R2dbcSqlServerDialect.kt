package org.komapper.dialect.sqlserver.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.AtSignBinder
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcArrayType
import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
import org.komapper.r2dbc.R2dbcBooleanType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
import org.komapper.r2dbc.R2dbcClobType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDoubleType
import org.komapper.r2dbc.R2dbcFloatType
import org.komapper.r2dbc.R2dbcIntType
import org.komapper.r2dbc.R2dbcLocalDateTimeType
import org.komapper.r2dbc.R2dbcLocalDateType
import org.komapper.r2dbc.R2dbcLocalTimeType
import org.komapper.r2dbc.R2dbcLongType
import org.komapper.r2dbc.R2dbcShortType
import org.komapper.r2dbc.R2dbcStringType
import org.komapper.r2dbc.R2dbcUByteType
import org.komapper.r2dbc.R2dbcUIntType
import org.komapper.r2dbc.R2dbcUShortType

class R2dbcSqlServerDialect(
    dataTypes: List<R2dbcDataType<*>> = emptyList()
) : SqlServerDialect, R2dbcAbstractDialect(defaultDataTypes + dataTypes) {

    companion object {
        val defaultDataTypes: List<R2dbcDataType<*>> = listOf(
            R2dbcArrayType("array"),
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("varbinary(max)"),
            R2dbcBooleanType("bit"),
            R2dbcByteType("smallint"),
            R2dbcByteArrayType("varbinary(1000)"),
            R2dbcClobType("text"),
            R2dbcDoubleType("real"),
            R2dbcFloatType("float"),
            R2dbcIntType("int"),
            R2dbcLocalDateTimeType("datetime"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(1000)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("int"),
            R2dbcSqlServerBooleanType,
        )
    }

    override fun getBinder(): Binder {
        return AtSignBinder
    }

    override fun isSequenceExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
    }

    override fun isTableExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}

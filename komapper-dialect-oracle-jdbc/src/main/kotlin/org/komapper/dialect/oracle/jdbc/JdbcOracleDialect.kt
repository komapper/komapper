package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
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
import org.komapper.jdbc.JdbcOffsetDateTimeType
import org.komapper.jdbc.JdbcShortType
import org.komapper.jdbc.JdbcStringType
import org.komapper.jdbc.JdbcUByteType
import org.komapper.jdbc.JdbcUIntType
import org.komapper.jdbc.JdbcUShortType
import java.sql.SQLException

class JdbcOracleDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
) : OracleDialect, JdbcAbstractDialect(DEFAULT_DATA_TYPES + dataTypes) {

    companion object {
        val DEFAULT_DATA_TYPES: List<JdbcDataType<*>> = listOf(
            JdbcBigDecimalType("decimal"),
            JdbcBigIntegerType("decimal"),
            JdbcBlobType("blob"),
            JdbcByteType("integer"),
            JdbcByteArrayType("raw"),
            JdbcClobType("clob"),
            JdbcDoubleType("float"),
            JdbcFloatType("float"),
            JdbcIntType("integer"),
            JdbcLocalDateTimeType("timestamp"),
            JdbcLocalDateType("date"),
            JdbcLocalTimeType("date"),
            JdbcLongType("integer"),
            JdbcOffsetDateTimeType("timestamp with time zone"),
            JdbcShortType("integer"),
            JdbcStringType("varchar2(1000)"),
            JdbcUByteType("integer"),
            JdbcUIntType("integer"),
            JdbcUShortType("integer"),
            JdbcOracleBooleanType
        )
    }

    override fun isSequenceExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
        }
    }

    override fun isSequenceNotExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.SEQUENCE_NOT_EXISTS_ERROR_CODE
        }
    }

    override fun isTableExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
        }
    }

    override fun isTableNotExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.TABLE_NOT_EXISTS_ERROR_CODE
        }
    }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun supportsReturnGeneratedKeysFlag(): Boolean = false
}

package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.AbstractJdbcDialect
import org.komapper.jdbc.AnyType
import org.komapper.jdbc.ArrayType
import org.komapper.jdbc.BigDecimalType
import org.komapper.jdbc.BigIntegerType
import org.komapper.jdbc.BlobType
import org.komapper.jdbc.BooleanType
import org.komapper.jdbc.ByteArrayType
import org.komapper.jdbc.ByteType
import org.komapper.jdbc.ClobType
import org.komapper.jdbc.DoubleType
import org.komapper.jdbc.FloatType
import org.komapper.jdbc.IntType
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.LocalDateTimeType
import org.komapper.jdbc.LocalDateType
import org.komapper.jdbc.LocalTimeType
import org.komapper.jdbc.LongType
import org.komapper.jdbc.NClobType
import org.komapper.jdbc.OffsetDateTimeType
import org.komapper.jdbc.SQLXMLType
import org.komapper.jdbc.ShortType
import org.komapper.jdbc.StringType
import org.komapper.jdbc.UByteType
import org.komapper.jdbc.UIntType
import org.komapper.jdbc.UShortType
import java.sql.SQLException

open class H2JdbcDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.IMPLICIT
) : H2Dialect, AbstractJdbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505

        val defaultDataTypes: List<JdbcDataType<*>> = listOf(
            AnyType("other"),
            ArrayType("array"),
            BigDecimalType("bigint"),
            BigIntegerType("bigint"),
            BlobType("blob"),
            BooleanType("bool"),
            ByteType("tinyint"),
            ByteArrayType("binary"),
            DoubleType("double"),
            ClobType("clob"),
            FloatType("float"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            NClobType("nclob"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            SQLXMLType("clob"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
            H2UUIDType
        )
    }

    enum class Version { IMPLICIT }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }
}

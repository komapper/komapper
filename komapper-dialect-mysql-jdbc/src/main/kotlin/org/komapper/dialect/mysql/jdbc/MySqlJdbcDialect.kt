package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.AbstractJdbcDialect
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

open class MySqlJdbcDialect(
    dataTypes: List<JdbcDataType<*>> = emptyList(),
    val version: Version = Version.V8_0
) : MySqlDialect, AbstractJdbcDialect(defaultDataTypes + dataTypes) {

    companion object {
        enum class Version { V8_0 }

        /** the error code that represents unique violation  */
        var UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES = setOf(1022, 1062)

        val defaultDataTypes: List<JdbcDataType<*>> = listOf(
            ArrayType("varbinary(500)"),
            BigDecimalType("decimal"),
            BigIntegerType("decimal"),
            BlobType("blob"),
            BooleanType("bit(1)"),
            ByteType("tinyint"),
            ByteArrayType("bytea"),
            DoubleType("double precision"),
            ClobType("text"),
            FloatType("real"),
            IntType("integer"),
            LocalDateTimeType("timestamp(6)"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            NClobType("text"),
            OffsetDateTimeType("timestamp"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            SQLXMLType("text"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
        )
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode in UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }
}

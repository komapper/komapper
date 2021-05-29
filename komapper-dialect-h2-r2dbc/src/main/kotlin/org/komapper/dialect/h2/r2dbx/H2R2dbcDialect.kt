package org.komapper.dialect.h2.r2dbx

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.AnyType
import org.komapper.r2dbc.ArrayType
import org.komapper.r2dbc.BigDecimalType
import org.komapper.r2dbc.BigIntegerType
import org.komapper.r2dbc.BooleanType
import org.komapper.r2dbc.ByteArrayType
import org.komapper.r2dbc.ByteType
import org.komapper.r2dbc.DataType
import org.komapper.r2dbc.DoubleType
import org.komapper.r2dbc.FloatType
import org.komapper.r2dbc.IntType
import org.komapper.r2dbc.LocalDateTimeType
import org.komapper.r2dbc.LocalDateType
import org.komapper.r2dbc.LocalTimeType
import org.komapper.r2dbc.LongType
import org.komapper.r2dbc.OffsetDateTimeType
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.ShortType
import org.komapper.r2dbc.StringType
import org.komapper.r2dbc.UByteType
import org.komapper.r2dbc.UIntType
import org.komapper.r2dbc.UShortType
import kotlin.reflect.KClass

open class H2R2dbcDialect(val version: Version = Version.V0_8) : H2Dialect, R2dbcDialect {

    private val dataTypeMap = defaultDataTypes.associateBy { it.klass }

    companion object {
        enum class Version { V0_8 }

        const val driver = "h2"

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505

        val defaultDataTypes: List<DataType<*>> = listOf(
            AnyType("other"),
            ArrayType("array"),
            BigDecimalType("bigint"),
            BigIntegerType("bigint"),
            BooleanType("bool"),
            ByteType("tinyint"),
            ByteArrayType("binary"),
            DoubleType("double"),
            FloatType("float"),
            IntType("integer"),
            LocalDateTimeType("timestamp"),
            LocalDateType("date"),
            LocalTimeType("time"),
            LongType("bigint"),
            OffsetDateTimeType("timestamp with time zone"),
            ShortType("smallint"),
            StringType("varchar(500)"),
            UByteType("smallint"),
            UIntType("bigint"),
            UShortType("integer"),
        )
    }

    override val driver: String = Companion.driver

    override fun formatValue(value: Any?, valueClass: KClass<*>): String {
        return value.toString()
    }

    // TODO
    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }

    // TODO
    override fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>) {
        if (value == null) {
            statement.bindNull(index, valueClass.java)
        } else {
            statement.bind(index, value)
        }
    }

    override fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, index)
    }

    override fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, columnLabel)
    }

    private fun getDataType(klass: KClass<*>): DataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }
}

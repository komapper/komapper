package org.komapper.dialect.postgresql.r2dbc

import org.komapper.r2dbc.R2dbcBigDecimalType
import org.komapper.r2dbc.R2dbcBigIntegerType
import org.komapper.r2dbc.R2dbcBlobType
import org.komapper.r2dbc.R2dbcBooleanType
import org.komapper.r2dbc.R2dbcByteArrayType
import org.komapper.r2dbc.R2dbcByteType
import org.komapper.r2dbc.R2dbcClobStringType
import org.komapper.r2dbc.R2dbcClobType
import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDoubleType
import org.komapper.r2dbc.R2dbcFloatType
import org.komapper.r2dbc.R2dbcInstantAsTimestampWithTimezoneType
import org.komapper.r2dbc.R2dbcIntType
import org.komapper.r2dbc.R2dbcKotlinInstantAsTimestampWithTimezoneType
import org.komapper.r2dbc.R2dbcLocalDateTimeType
import org.komapper.r2dbc.R2dbcLocalDateType
import org.komapper.r2dbc.R2dbcLocalTimeType
import org.komapper.r2dbc.R2dbcLongType
import org.komapper.r2dbc.R2dbcOffsetDateTimeType
import org.komapper.r2dbc.R2dbcShortType
import org.komapper.r2dbc.R2dbcStringType
import org.komapper.r2dbc.R2dbcUByteType
import org.komapper.r2dbc.R2dbcUIntType
import org.komapper.r2dbc.R2dbcUShortType
import kotlin.reflect.KClass
import kotlin.reflect.KType

class PostgreSqlR2dbcDataTypeProvider(private val next: R2dbcDataTypeProvider) :
    R2dbcDataTypeProvider {
    companion object {
        val DEFAULT_DATA_TYPES: List<R2dbcDataType<*>> = listOf(
            R2dbcBigDecimalType("decimal"),
            R2dbcBigIntegerType("decimal"),
            R2dbcBlobType("bytea"),
            R2dbcBooleanType("boolean"),
            R2dbcByteType("smallint"),
            R2dbcByteArrayType("bytea"),
            R2dbcClobType("text"),
            R2dbcClobStringType("text"),
            R2dbcDoubleType("double precision"),
            R2dbcFloatType("real"),
            R2dbcInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            R2dbcIntType("integer"),
            R2dbcKotlinInstantAsTimestampWithTimezoneType("timestamp with time zone"),
            R2dbcLocalDateTimeType("timestamp"),
            R2dbcLocalDateType("date"),
            R2dbcLocalTimeType("time"),
            R2dbcLongType("bigint"),
            R2dbcOffsetDateTimeType("timestamp with time zone"),
            R2dbcShortType("smallint"),
            R2dbcStringType("varchar(500)"),
            R2dbcUByteType("smallint"),
            R2dbcUIntType("bigint"),
            R2dbcUShortType("integer"),
            PostgreSqlR2dbcBoxType,
            PostgreSqlR2dbcCircleType,
            PostgreSqlR2dbcGeometryType,
            PostgreSqlR2dbcIntervalType,
            PostgreSqlR2dbcJsonType,
            PostgreSqlR2dbcLineType,
            PostgreSqlR2dbcLsegType,
            PostgreSqlR2dbcPathType,
            PostgreSqlR2dbcPointType,
            PostgreSqlR2dbcPolygonType,
            PostgreSqlR2dbcUUIDType,
        )
    }

    private val dataTypeMap: Map<KType, R2dbcDataType<*>> = DEFAULT_DATA_TYPES.associateBy { it.type }

    private val dataTypeMapByKClass: Map<KClass<*>, R2dbcDataType<*>> =
        DEFAULT_DATA_TYPES.associateBy { it.type.classifier as KClass<*> }

    override fun <T : Any> get(type: KType): R2dbcDataType<T>? {
        @Suppress("UNCHECKED_CAST")
        val dataType = dataTypeMap[type] as R2dbcDataType<T>?
        return dataType ?: getArrayDataType(type) ?: next.get(type)
    }

    /**
     * Determines the appropriate R2dbcDataType for an array type based on the given KType.
     *
     * Note that the classifier of an Array type with primitive boxed elements returns a specialized xxxArray for the corresponding primitive type.
     *
     * @param type the KType representing the array data type to be processed
     * @return the corresponding R2dbcDataType for the array type, or null if no suitable type is found
     */
    private fun <T : Any> getArrayDataType(type: KType): R2dbcDataType<T>? {
        return when (val klass = type.classifier as KClass<*>) {
            // CharArray and ByteArray are not supported.
            BooleanArray::class -> createArrayType(Any::class, Boolean::class)
            DoubleArray::class -> createArrayType(Any::class, Double::class)
            FloatArray::class -> createArrayType(Any::class, Float::class)
            IntArray::class -> createArrayType(Any::class, Int::class)
            LongArray::class -> createArrayType(Any::class, Long::class)
            ShortArray::class -> createArrayType(Any::class, Short::class)
            else -> {
                if (Array::class.java.isAssignableFrom(klass.java)) {
                    val componentType = klass.java.componentType.kotlin
                    createArrayType(klass, componentType)
                } else {
                    null
                }
            }
        }
    }

    private fun <T : Any> createArrayType(arrayType: KClass<*>, componentType: KClass<*>): R2dbcDataType<T>? {
        // If the componentType can be converted to KType, the getDataType function should be called
        val componentDataType = dataTypeMapByKClass[componentType]
        checkNotNull(componentDataType) { "The dataType is not found for the component type \"${componentType.qualifiedName}\"." }
        @Suppress("UNCHECKED_CAST")
        return PostgreSqlR2dbcArrayType(arrayType, componentDataType) as R2dbcDataType<T>?
    }
}

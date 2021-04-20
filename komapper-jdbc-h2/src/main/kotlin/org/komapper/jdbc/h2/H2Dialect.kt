package org.komapper.jdbc.h2

import org.komapper.core.AbstractDialect
import org.komapper.core.dsl.builder.EntityMultiUpsertStatementBuilder
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.jdbc.AnyType
import org.komapper.core.jdbc.ArrayType
import org.komapper.core.jdbc.BigDecimalType
import org.komapper.core.jdbc.BigIntegerType
import org.komapper.core.jdbc.BlobType
import org.komapper.core.jdbc.BooleanType
import org.komapper.core.jdbc.ByteArrayType
import org.komapper.core.jdbc.ByteType
import org.komapper.core.jdbc.ClobType
import org.komapper.core.jdbc.DataType
import org.komapper.core.jdbc.DoubleType
import org.komapper.core.jdbc.FloatType
import org.komapper.core.jdbc.IntType
import org.komapper.core.jdbc.LocalDateTimeType
import org.komapper.core.jdbc.LocalDateType
import org.komapper.core.jdbc.LocalTimeType
import org.komapper.core.jdbc.LongType
import org.komapper.core.jdbc.NClobType
import org.komapper.core.jdbc.OffsetDateTimeType
import org.komapper.core.jdbc.SQLXMLType
import org.komapper.core.jdbc.ShortType
import org.komapper.core.jdbc.StringType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLException
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

open class H2Dialect(val version: Version = Version.V1_4) : AbstractDialect() {

    companion object {
        enum class Version { V1_4 }

        /** the error code that represents unique violation  */
        const val UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE = 23505
    }

    override fun isUniqueConstraintViolation(exception: SQLException): Boolean {
        val cause = getCause(exception)
        return cause.errorCode == UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun getSequenceSql(sequenceName: String): String {
        return "call next value for $sequenceName"
    }

    @Suppress("UNCHECKED_CAST")
    override fun getDataType(type: KClass<*>): Pair<DataType<*>, String> = when (type) {
        Any::class -> AnyType to "other"
        java.sql.Array::class -> ArrayType to "array"
        BigDecimal::class -> BigDecimalType to "bigint"
        BigInteger::class -> BigIntegerType to "bigint"
        Blob::class -> BlobType to "blob"
        Boolean::class -> BooleanType to "bool"
        Byte::class -> ByteType to "tinyint"
        ByteArray::class -> ByteArrayType to "binary"
        Double::class -> DoubleType to "double"
        Clob::class -> ClobType to "clob"
        Float::class -> FloatType to "float"
        Int::class -> IntType to "integer"
        LocalDateTime::class -> LocalDateTimeType to "timestamp"
        LocalDate::class -> LocalDateType to "date"
        LocalTime::class -> LocalTimeType to "time"
        Long::class -> LongType to "bigint"
        NClob::class -> NClobType to "nclob"
        OffsetDateTime::class -> OffsetDateTimeType to "timestamp with time zone"
        Short::class -> ShortType to "smallint"
        String::class -> StringType to "varchar(500)"
        SQLXML::class -> SQLXMLType to "clob"
        else -> error(
            "The dataType is not found for the type \"${type.qualifiedName}\"."
        )
    }

    override fun getSchemaStatementBuilder(): SchemaStatementBuilder {
        return H2SchemaStatementBuilder(this)
    }

    override fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entity: ENTITY
    ): EntityUpsertStatementBuilder<ENTITY> {
        return H2EntityMultiUpsertStatementBuilder(this, context, listOf(entity))
    }

    override fun <ENTITY : Any, META : EntityMetamodel<ENTITY, META>> getEntityMultiUpsertStatementBuilder(
        context: EntityUpsertContext<ENTITY, META>,
        entities: List<ENTITY>
    ): EntityMultiUpsertStatementBuilder<ENTITY> {
        return H2EntityMultiUpsertStatementBuilder(this, context, entities)
    }
}

package org.komapper.exposed

import org.jetbrains.exposed.v1.core.BasicBinaryColumnType
import org.jetbrains.exposed.v1.core.BlobColumnType
import org.jetbrains.exposed.v1.core.BooleanColumnType
import org.jetbrains.exposed.v1.core.ByteColumnType
import org.jetbrains.exposed.v1.core.CharacterColumnType
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.IntegerColumnType
import org.jetbrains.exposed.v1.core.LongColumnType
import org.jetbrains.exposed.v1.core.ShortColumnType
import org.jetbrains.exposed.v1.core.UByteColumnType
import org.jetbrains.exposed.v1.core.UIntegerColumnType
import org.jetbrains.exposed.v1.core.ULongColumnType
import org.jetbrains.exposed.v1.core.UShortColumnType
import org.jetbrains.exposed.v1.core.UUIDColumnType
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.komapper.core.BuilderDialect
import org.komapper.core.DataType
import org.komapper.core.dsl.builder.EntityUpsertStatementBuilder
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class Argument<T>(
    val name: String,
    val value: T?,
    val type: KType,
    val columnType: IColumnType<T>,
) : CharSequence {
    override val length: Int get() = name.length
    override fun get(index: Int): Char = name[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = name.subSequence(startIndex, endIndex)
    override fun toString(): String = name
}

fun resolveColumnType(type: KType): IColumnType<*> {
    return when (type) {
        typeOf<String>() -> VarCharColumnType()
        typeOf<Byte>() -> ByteColumnType()
        typeOf<UByte>() -> UByteColumnType()
        typeOf<Short>() -> ShortColumnType()
        typeOf<UShort>() -> UShortColumnType()
        typeOf<Int>() -> IntegerColumnType()
        typeOf<UInt>() -> UIntegerColumnType()
        typeOf<Long>() -> LongColumnType()
        typeOf<ULong>() -> ULongColumnType()
        typeOf<Boolean>() -> BooleanColumnType()
        typeOf<ByteArray>() -> BasicBinaryColumnType()
        typeOf<ExposedBlob>() -> BlobColumnType()
        typeOf<Char>() -> CharacterColumnType()
        typeOf<Float>() -> FloatColumnType()
        typeOf<Double>() -> DoubleColumnType()
        typeOf<UUID>() -> UUIDColumnType()

        else -> throw UnsupportedOperationException("Unsupported type: $type")
    }
}

class SqlTemplateDialect() : BuilderDialect {
    override val driver: String = "komapper-exposed"

    override fun getSequenceSql(sequenceName: String): String {
        throw UnsupportedOperationException()
    }

    override fun getSchemaStatementBuilder(dialect: BuilderDialect): SchemaStatementBuilder {
        throw UnsupportedOperationException()
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> getEntityUpsertStatementBuilder(
        dialect: BuilderDialect,
        context: EntityUpsertContext<ENTITY, ID, META>,
        entities: List<ENTITY>,
    ): EntityUpsertStatementBuilder<ENTITY> {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> formatValue(
        value: T?,
        type: KType,
        masking: Boolean,
    ): String {
        return value.toString()
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getDataType(type: KType): DataType {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getDataTypeOrNull(type: KType): DataType {
        throw UnsupportedOperationException()
    }
}

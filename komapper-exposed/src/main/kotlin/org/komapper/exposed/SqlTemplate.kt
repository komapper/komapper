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
import java.util.UUID
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Represents a SQL template argument that binds a value to a named parameter.
 *
 * This class implements [CharSequence] to allow seamless integration with SQL template strings,
 * where the argument name is used as the parameter placeholder.
 *
 * @param T the type of the value
 * @param name the parameter name used in the SQL template
 * @param value the actual value to bind (nullable)
 * @param type the Kotlin type information
 * @param columnType the Exposed column type for proper JDBC/R2DBC binding
 */
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

/**
 * Resolves the appropriate Exposed column type for a given Kotlin type.
 *
 * This function maps Kotlin types to their corresponding Exposed [IColumnType] implementations,
 * which are used for proper JDBC/R2DBC binding and database operations.
 *
 * Supported types:
 * - Primitive types: String, Byte, Short, Int, Long, Boolean, Char, Float, Double
 * - Unsigned types: UByte, UShort, UInt, ULong
 * - Binary types: ByteArray, ExposedBlob
 * - UUID
 *
 * @param type the Kotlin type to resolve
 * @return the corresponding Exposed column type
 * @throws UnsupportedOperationException if the type is not supported
 */
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

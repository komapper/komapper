package org.komapper.core.dsl.expression

import kotlin.reflect.KType

internal sealed interface LiteralExpression<EXTERNAL : Any, INTERNAL : Any> : ColumnExpression<EXTERNAL, INTERNAL>

data class NullLiteralExpression<EXTERNAL : Any, INTERNAL : Any>(
    override val exteriorType: KType,
    override val interiorType: KType,
) :
    LiteralExpression<EXTERNAL, INTERNAL> {
    override val owner: TableExpression<*>
        get() = throw UnsupportedOperationException()
    override val wrap: (INTERNAL) -> EXTERNAL
        get() = throw UnsupportedOperationException()
    override val unwrap: (EXTERNAL) -> INTERNAL
        get() = throw UnsupportedOperationException()
    override val columnName: String
        get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = false
    override val masking: Boolean get() = false
}

internal data class NullableLiteralExpression<EXTERNAL : Any, INTERNAL : Any>(
    val value: INTERNAL?,
    override val exteriorType: KType,
    override val interiorType: KType,
    override val wrap: (INTERNAL) -> EXTERNAL,
    override val unwrap: (EXTERNAL) -> INTERNAL,
) : LiteralExpression<EXTERNAL, INTERNAL> {
    override val owner: TableExpression<*>
        get() = throw UnsupportedOperationException()
    override val columnName: String
        get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = false
    override val masking: Boolean get() = false
}

fun <T : Any> createSimpleNullableLiteralExpression(value: T?, type: KType): ColumnExpression<T, T> {
    return NullableLiteralExpression(value, type, type, { it }, { it })
}

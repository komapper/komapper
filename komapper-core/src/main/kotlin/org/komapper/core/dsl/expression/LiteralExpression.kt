package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

internal sealed interface LiteralExpression<EXTERNAL : Any, INTERNAL : Any> : ColumnExpression<EXTERNAL, INTERNAL>

data class NullLiteralExpression<EXTERNAL : Any, INTERNAL : Any>(
    override val exteriorClass: KClass<EXTERNAL>,
    override val interiorClass: KClass<INTERNAL>,
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

internal data class NonNullLiteralExpression<T : Any>(
    val value: T,
    private val klass: KClass<T>,
) : LiteralExpression<T, T> {
    override val owner: TableExpression<*>
        get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<T> = klass
    override val interiorClass: KClass<T> = klass
    override val wrap: (T) -> T get() = { it }
    override val unwrap: (T) -> T get() = { it }
    override val columnName: String
        get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = false
    override val masking: Boolean get() = false
}

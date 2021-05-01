package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

internal class LiteralExpression<T : Any>(val value: T, expression: ColumnExpression<T, T>) :
    ColumnExpression<T, T> by expression

private class GenericExpression<T : Any>(klass: KClass<T>) : ColumnExpression<T, T> {
    override val owner: TableExpression<*>
        get() = throw UnsupportedOperationException()
    override val exteriorClass: KClass<T> = klass
    override val interiorClass: KClass<T> = klass
    override val wrap: (T) -> T get() = { it }
    override val unwrap: (T) -> T get() = { it }
    override val columnName: String
        get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = false
}

internal object BooleanExpression : ColumnExpression<Boolean, Boolean> by GenericExpression(Boolean::class)
internal object IntExpression : ColumnExpression<Int, Int> by GenericExpression(Int::class)
internal object LongExpression : ColumnExpression<Long, Long> by GenericExpression(Long::class)
internal object StringExpression : ColumnExpression<String, String> by GenericExpression(String::class)

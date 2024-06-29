package org.komapper.core.dsl.expression

import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal sealed class StringFunction<T : Any, S : Any> : ColumnExpression<T, S> {
    internal data class Concat<T : Any>(
        val expression: ColumnExpression<T, String>,
        val left: Operand,
        val right: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Locate(
        val pattern: Operand,
        val string: Operand,
        val startIndex: Operand?,
    ) : StringFunction<Int, Int>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val exteriorType: KType = typeOf<Int>()
        override val interiorType: KType = typeOf<Int>()
        override val wrap: (Int) -> Int get() = { it }
        override val unwrap: (Int) -> Int get() = { it }
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
        override val masking: Boolean get() = throw UnsupportedOperationException()
    }

    internal data class Lower<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Ltrim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Rtrim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Substring<T : Any>(
        val expression: ColumnExpression<T, String>,
        val target: Operand,
        val startIndex: Operand,
        val length: Operand?,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Trim<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()

    internal data class Upper<T : Any>(
        val expression: ColumnExpression<T, String>,
        val operand: Operand,
    ) :
        ColumnExpression<T, String> by expression, StringFunction<T, String>()
}

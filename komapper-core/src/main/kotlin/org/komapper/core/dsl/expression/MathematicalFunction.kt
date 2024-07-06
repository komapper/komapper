package org.komapper.core.dsl.expression

import java.math.BigDecimal
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal sealed class MathematicalFunction<T : Any, S : Any> : ColumnExpression<T, S> {
    internal object Random : MathematicalFunction<BigDecimal, BigDecimal>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val exteriorType: KType = typeOf<BigDecimal>()
        override val interiorType: KType = typeOf<BigDecimal>()
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
        override val masking: Boolean get() = throw UnsupportedOperationException()
        override val wrap: (BigDecimal) -> BigDecimal = { it }
        override val unwrap: (BigDecimal) -> BigDecimal = { it }
    }
}

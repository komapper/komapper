package org.komapper.core.dsl.expression

import java.math.BigDecimal
import kotlin.reflect.KClass

internal sealed class MathematicalFunction<T : Any, S : Any> : ColumnExpression<T, S> {
    internal object Random : MathematicalFunction<BigDecimal, BigDecimal>() {
        override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
        override val exteriorClass: KClass<BigDecimal> get() = BigDecimal::class
        override val interiorClass: KClass<BigDecimal> get() = BigDecimal::class
        override val columnName: String get() = throw UnsupportedOperationException()
        override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
        override val masking: Boolean get() = throw UnsupportedOperationException()
        override val wrap: (BigDecimal) -> BigDecimal = { it }
        override val unwrap: (BigDecimal) -> BigDecimal = { it }
    }
}

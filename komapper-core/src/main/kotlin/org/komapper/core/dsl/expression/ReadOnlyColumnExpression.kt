package org.komapper.core.dsl.expression

import kotlin.reflect.KType

class ReadOnlyColumnExpression<EXTERIOR : Any, INTERIOR : Any>(
    override val owner: TableExpression<*>,
    override val exteriorType: KType,
    override val interiorType: KType,
    override val wrap: (INTERIOR) -> EXTERIOR,
    override val columnName: String,
    override val alwaysQuote: Boolean,
    override val masking: Boolean,
) : ColumnExpression<EXTERIOR, INTERIOR> {
    override val unwrap: (EXTERIOR) -> INTERIOR
        get() = throw UnsupportedOperationException()
}

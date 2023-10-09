package org.komapper.core.dsl.expression

import kotlin.reflect.KClass

internal class UserDefinedExpression<EXTERIOR : Any, INTERIOR : Any>(
    override val exteriorClass: KClass<EXTERIOR>,
    override val interiorClass: KClass<INTERIOR>,
    override val wrap: (INTERIOR) -> EXTERIOR,
    private val name: String,
    private val operands: List<Operand>,
    private val build: SqlBuilderScope.() -> Unit,
) : ColumnExpression<EXTERIOR, INTERIOR> {
    override val unwrap: (EXTERIOR) -> INTERIOR get() = throw UnsupportedOperationException()
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
    fun build(scope: SqlBuilderScope) {
        scope.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDefinedExpression<*, *>

        if (exteriorClass != other.exteriorClass) return false
        if (interiorClass != other.interiorClass) return false
        if (name != other.name) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exteriorClass.hashCode()
        result = 31 * result + interiorClass.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}

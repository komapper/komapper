package org.komapper.core.dsl.expression

import kotlin.reflect.KType

interface UserDefinedExpression<EXTERIOR : Any, INTERIOR : Any> : ColumnExpression<EXTERIOR, INTERIOR> {
    val name: String
    val operands: List<Operand>
    fun build(scope: SqlBuilderScope)
}

internal class UserDefinedExpressionImpl<EXTERIOR : Any, INTERIOR : Any>(
    override val exteriorType: KType,
    override val interiorType: KType,
    override val wrap: (INTERIOR) -> EXTERIOR,
    override val name: String,
    override val operands: List<Operand>,
    private val build: SqlBuilderScope.() -> Unit,
) : UserDefinedExpression<EXTERIOR, INTERIOR> {
    override val unwrap: (EXTERIOR) -> INTERIOR get() = throw UnsupportedOperationException()
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
    override fun build(scope: SqlBuilderScope) {
        scope.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDefinedExpression<*, *>

        if (exteriorType != other.exteriorType) return false
        if (interiorType != other.interiorType) return false
        if (name != other.name) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exteriorType.hashCode()
        result = 31 * result + interiorType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}

fun <EXTERIOR : Any, INTERIOR : Any> UserDefinedExpression(
    exteriorType: KType,
    interiorType: KType,
    wrap: (INTERIOR) -> EXTERIOR,
    name: String,
    operands: List<Operand>,
    build: SqlBuilderScope.() -> Unit,
): UserDefinedExpression<EXTERIOR, INTERIOR> {
    return UserDefinedExpressionImpl(exteriorType, interiorType, wrap, name, operands, build)
}

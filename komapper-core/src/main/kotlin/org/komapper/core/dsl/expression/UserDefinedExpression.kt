package org.komapper.core.dsl.expression

import org.komapper.core.Dialect
import org.komapper.core.StatementBuffer
import kotlin.reflect.KClass

internal class UserDefinedExpression<EXTERIOR : Any, INTERIOR : Any>(
    override val exteriorClass: KClass<EXTERIOR>,
    override val interiorClass: KClass<INTERIOR>,
    override val wrap: (INTERIOR) -> EXTERIOR,
    private val operands: List<Operand>,
    private val builder: SqlBuilderScope.() -> Unit,
) : ColumnExpression<EXTERIOR, INTERIOR> {
    override val unwrap: (EXTERIOR) -> INTERIOR get() = throw UnsupportedOperationException()
    override val owner: TableExpression<*> get() = throw UnsupportedOperationException()
    override val columnName: String get() = throw UnsupportedOperationException()
    override val alwaysQuote: Boolean get() = throw UnsupportedOperationException()
    override val masking: Boolean get() = throw UnsupportedOperationException()
    fun build(scope: SqlBuilderScope) {
        scope.builder()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDefinedExpression<*, *>

        if (exteriorClass != other.exteriorClass) return false
        if (interiorClass != other.interiorClass) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exteriorClass.hashCode()
        result = 31 * result + interiorClass.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}

interface SqlBuilderScope {
    val dialect: Dialect
    fun append(text: CharSequence)
    fun cutBack(length: Int)
    fun visit(operand: Operand)
}

internal class SqlBuilderScopeImpl(
    override val dialect: Dialect,
    val buf: StatementBuffer,
    val visitOperand: (Operand) -> Unit,
) : SqlBuilderScope {
    override fun append(text: CharSequence) {
        buf.append(text)
    }

    override fun cutBack(length: Int) {
        buf.cutBack(length)
    }

    override fun visit(operand: Operand) {
        visitOperand(operand)
    }

    override fun toString(): String {
        return buf.toString()
    }
}

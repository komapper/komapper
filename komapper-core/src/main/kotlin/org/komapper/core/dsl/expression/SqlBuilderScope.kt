package org.komapper.core.dsl.expression

import org.komapper.core.Dialect
import org.komapper.core.StatementBuffer

/**
 * The scope for SQL building.
 */
interface SqlBuilderScope {

    val dialect: Dialect

    /**
     * Appends an SQL fragment.
     */
    fun append(text: CharSequence)

    /**
     * Deletes characters from the SQL string.
     *
     * @param length character length
     */
    fun cutBack(length: Int)

    /**
     * Processes an operand.
     */
    fun visit(operand: Operand)
}

internal class SqlBuilderScopeImpl(
    override val dialect: Dialect,
    private val buf: StatementBuffer,
    private val visitOperand: (Operand) -> Unit,
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

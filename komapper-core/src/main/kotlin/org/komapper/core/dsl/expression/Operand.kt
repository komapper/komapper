package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import kotlin.reflect.KType

/**
 * Represents operands in Komapper Query DSL.
 */
@ThreadSafe
sealed class Operand {
    /**
     * Whether data is masked in the log output.
     */
    abstract val masking: Boolean

    /**
     * A column.
     *
     * @param expression the column expression
     */
    data class Column(val expression: ColumnExpression<*, *>) : Operand() {
        override val masking: Boolean get() = expression.masking
    }

    /**
     * An argument to be bound to a prepared statement.
     *
     * @param expression the column expression
     * @param exterior the argument value
     */
    data class Argument<T : Any, S : Any>(val expression: ColumnExpression<T, S>, val exterior: T?) : Operand() {
        override val masking: Boolean get() = expression.masking

        /**
         * The bindable format of the argument.
         */
        val value: Value<S> get() {
            val interior = if (exterior == null) null else expression.unwrap(exterior)
            return Value(interior, expression.interiorType, expression.masking)
        }
    }

    /**
     * A simple argument to be bound to a prepared statement.
     *
     * @param interiorType the interior type
     * @param interior the argument value
     */
    data class SimpleArgument<S : Any>(val interiorType: KType, val interior: S?) : Operand() {
        override val masking: Boolean get() = false

        /**
         * The bindable format of the argument.
         */
        val value: Value<S> get() {
            return Value(interior, interiorType, masking)
        }
    }

    /**
     * An escaped argument to be bound to a prepared statement.
     *
     * @param expression the column expression
     * @param escapeExpression the escaped expression
     */
    data class Escape(val expression: ColumnExpression<*, *>, val escapeExpression: EscapeExpression) : Operand() {
        override val masking: Boolean get() = expression.masking
    }

    /**
     * A subquery.
     */
    data class Subquery(val subqueryExpression: SubqueryExpression<*>) : Operand() {
        override val masking: Boolean = false
    }
}

package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.ReadOnlyColumnExpression
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.UserDefinedExpression
import kotlin.reflect.typeOf

/**
 * Define a new column expression.
 *
 * The [name] and [operands] are used to determine identity of the expression.
 *
 * @param T the exterior type of expression evaluation
 * @param S the interior type of expression evaluation
 * @param baseExpression the base column expression
 * @param name the name that must be unique among user-defined column expressions
 * @param operands the operand list used in the expression
 * @param build the SQL builder
 * @return column expression
 */
inline fun <reified T : Any, reified S : Any> columnExpression(
    baseExpression: ColumnExpression<T, S>,
    name: String,
    operands: List<Operand>,
    noinline build: SqlBuilderScope.() -> Unit,
): ColumnExpression<T, S> {
    return columnExpression(
        baseExpression.wrap,
        name,
        operands,
        build,
    )
}

/**
 * Define a new column expression.
 *
 * The [name] and [operands] are used to determine identity of the expression.
 *
 * @param T the exterior type and interior type of expression evaluation
 * @param name the name that must be unique among user-defined column expressions
 * @param operands the operand list used in the expression
 * @param build the SQL builder
 * @return column expression
 */
inline fun <reified T : Any> columnExpression(
    name: String,
    operands: List<Operand>,
    noinline build: SqlBuilderScope.() -> Unit,
): ColumnExpression<T, T> {
    return columnExpression({ it }, name, operands, build)
}

/**
 * Define a new column expression.
 *
 * The [name] and [operands] are used to determine identity of the expression.
 *
 * @param EXTERIOR the exterior type of expression evaluation
 * @param INTERIOR the interior type of expression evaluation
 * @param wrap the function to convert an interior value to an exterior value
 * @param name the name that must be unique among user-defined column expressions
 * @param operands the operand list used in the expression
 * @param build the SQL builder
 * @return column expression
 */
inline fun <reified EXTERIOR : Any, reified INTERIOR : Any> columnExpression(
    noinline wrap: (INTERIOR) -> EXTERIOR,
    name: String,
    operands: List<Operand>,
    noinline build: SqlBuilderScope.() -> Unit,
): ColumnExpression<EXTERIOR, INTERIOR> {
    return UserDefinedExpression(typeOf<EXTERIOR>(), typeOf<INTERIOR>(), wrap, name, operands, build)
}

/**
 * Transforms a [ColumnExpression<*, IN_INTERIOR>] to a [ColumnExpression<OUT_EXTERIOR, *>].
 *
 * @param wrap the mapping function
 */
inline fun <reified OUT_EXTERIOR : Any, IN_INTERIOR : Any> ColumnExpression<*, IN_INTERIOR>.transform(
    noinline wrap: (IN_INTERIOR) -> OUT_EXTERIOR,
): ColumnExpression<OUT_EXTERIOR, *> {
    return ReadOnlyColumnExpression(
        owner = this.owner,
        exteriorType = typeOf<OUT_EXTERIOR>(),
        interiorType = this.interiorType,
        wrap = wrap,
        alwaysQuote = this.alwaysQuote,
        columnName = this.columnName,
        masking = this.masking,
    )
}

package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.UserDefinedExpression
import kotlin.reflect.KClass

/**
 * Define a new simple column expression.
 *
 * The [operands] are used to determine identity of the expression.
 *
 * @param T the type of expression evaluation
 * @param klass the class of [T]
 * @param operands the operand list used in the expression
 * @param builder the SQL builder
 * @return column expression
 */
fun <T : Any> columnExpression(
    klass: KClass<T>,
    operands: List<Operand>,
    builder: SqlBuilderScope.() -> Unit,
): ColumnExpression<T, T> {
    return columnExpression(klass, klass, { it }, operands, builder)
}

/**
 * Define a new complex column expression.
 *
 * The [operands] are used to determine identity of the expression.
 *
 * @param EXTERIOR the exterior type of expression evaluation
 * @param INTERIOR the interior type of expression evaluation
 * @param exteriorClass the class of [EXTERIOR]
 * @param interiorClass the class of [INTERIOR]
 * @param wrap the function to convert an interior value to an exterior value
 * @param operands the operand list used in the expression
 * @param builder the SQL builder
 * @return column expression
 */
fun <EXTERIOR : Any, INTERIOR : Any> columnExpression(
    exteriorClass: KClass<EXTERIOR>,
    interiorClass: KClass<INTERIOR>,
    wrap: (INTERIOR) -> EXTERIOR,
    operands: List<Operand>,
    builder: SqlBuilderScope.() -> Unit,
): ColumnExpression<EXTERIOR, INTERIOR> {
    return UserDefinedExpression(exteriorClass, interiorClass, wrap, operands, builder)
}

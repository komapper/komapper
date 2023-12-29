package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.UserDefinedExpression
import kotlin.reflect.KClass

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
fun <T : Any, S : Any> columnExpression(
    baseExpression: ColumnExpression<T, S>,
    name: String,
    operands: List<Operand>,
    build: SqlBuilderScope.() -> Unit,
): ColumnExpression<T, S> {
    return columnExpression(baseExpression.exteriorClass, baseExpression.interiorClass, baseExpression.wrap, name, operands, build)
}

/**
 * Define a new column expression.
 *
 * The [name] and [operands] are used to determine identity of the expression.
 *
 * @param T the exterior type and interior type of expression evaluation
 * @param klass the class of [T]
 * @param name the name that must be unique among user-defined column expressions
 * @param operands the operand list used in the expression
 * @param build the SQL builder
 * @return column expression
 */
fun <T : Any> columnExpression(
    klass: KClass<T>,
    name: String,
    operands: List<Operand>,
    build: SqlBuilderScope.() -> Unit,
): ColumnExpression<T, T> {
    return columnExpression(klass, klass, { it }, name, operands, build)
}

/**
 * Define a new column expression.
 *
 * The [name] and [operands] are used to determine identity of the expression.
 *
 * @param EXTERIOR the exterior type of expression evaluation
 * @param INTERIOR the interior type of expression evaluation
 * @param exteriorClass the class of [EXTERIOR]
 * @param interiorClass the class of [INTERIOR]
 * @param wrap the function to convert an interior value to an exterior value
 * @param name the name that must be unique among user-defined column expressions
 * @param operands the operand list used in the expression
 * @param build the SQL builder
 * @return column expression
 */
fun <EXTERIOR : Any, INTERIOR : Any> columnExpression(
    exteriorClass: KClass<EXTERIOR>,
    interiorClass: KClass<INTERIOR>,
    wrap: (INTERIOR) -> EXTERIOR,
    name: String,
    operands: List<Operand>,
    build: SqlBuilderScope.() -> Unit,
): ColumnExpression<EXTERIOR, INTERIOR> {
    return UserDefinedExpression(exteriorClass, interiorClass, wrap, name, operands, build)
}

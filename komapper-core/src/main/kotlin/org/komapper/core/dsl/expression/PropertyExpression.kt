package org.komapper.core.dsl.expression

/**
 * Represents a property expression that maps an exterior type to an interior type.
 *
 * @param EXTERIOR the exterior type
 * @param INTERIOR the interior type
 */
interface PropertyExpression<EXTERIOR : Any, INTERIOR : Any> : ColumnExpression<EXTERIOR, INTERIOR>

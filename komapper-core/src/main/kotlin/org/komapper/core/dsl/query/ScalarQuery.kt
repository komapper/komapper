package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.ScalarQueryExpression
import org.komapper.core.dsl.expression.TableExpression
import kotlin.reflect.KClass

/**
 * Represents a query that returns a scalar.
 * ```kotlin
 * val a = Meta.address
 * val query: ScalarQuery<Long?, Long, Long> = QueryDsl.from(a).select(count())
 * ```
 */
interface ScalarQuery<A, B : Any, C : Any> : Query<A>, ScalarQueryExpression<A, B, C>

internal data class NullableScalarQuery<A, B : Any, C : Any>(
    val query: Subquery<A>,
    val expression: ScalarExpression<B, C>
) :
    ScalarQuery<A?, B, C>,
    Query<A?> by query.firstOrNull() {
    override val context: SubqueryContext
        get() = query.context
    override val owner: TableExpression<*>
        get() = expression.owner
    override val exteriorClass: KClass<B>
        get() = expression.exteriorClass
    override val interiorClass: KClass<C>
        get() = expression.interiorClass
    override val wrap: (C) -> B
        get() = expression.wrap
    override val unwrap: (B) -> C
        get() = expression.unwrap
    override val columnName: String
        get() = expression.columnName
    override val alwaysQuote: Boolean
        get() = expression.alwaysQuote
    override val masking: Boolean
        get() = expression.masking
}

internal data class NotNullScalarQuery<A, B : Any, C : Any>(
    val query: FlowSubquery<A>,
    val expression: ScalarExpression<B, C>
) :
    ScalarQuery<A, B, C>,
    Query<A> by query.first() {
    override val context: SubqueryContext
        get() = query.context
    override val owner: TableExpression<*>
        get() = expression.owner
    override val exteriorClass: KClass<B>
        get() = expression.exteriorClass
    override val interiorClass: KClass<C>
        get() = expression.interiorClass
    override val wrap: (C) -> B
        get() = expression.wrap
    override val unwrap: (B) -> C
        get() = expression.unwrap
    override val columnName: String
        get() = expression.columnName
    override val alwaysQuote: Boolean
        get() = expression.alwaysQuote
    override val masking: Boolean
        get() = expression.masking
}

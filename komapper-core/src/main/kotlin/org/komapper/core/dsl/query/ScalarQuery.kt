package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.ScalarQueryExpression
import org.komapper.core.dsl.expression.TableExpression
import kotlin.reflect.KClass

interface ScalarQuery<A, B : Any, C : Any> : FlowSubquery<A>, ScalarQueryExpression<A, B, C>

internal data class ScalarQueryImpl<A, B : Any, C : Any>(
    val query: FlowSubquery<A>,
    val expression: ScalarExpression<B, C>
) :
    ScalarQuery<A, B, C>,
    FlowSubquery<A> by query {
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

package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.CompositeColumnExpression

interface EmbeddedMetamodel<ENTITY : Any, EMBEDDABLE : Any> : CompositeColumnExpression<EMBEDDABLE> {
    fun properties(): List<PropertyMetamodel<ENTITY, *, *>>
}

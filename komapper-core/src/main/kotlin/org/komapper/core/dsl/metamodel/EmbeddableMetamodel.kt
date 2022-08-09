package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.CompositeColumnExpression

interface EmbeddableMetamodel<EMBEDDABLE : Any> : CompositeColumnExpression<EMBEDDABLE>

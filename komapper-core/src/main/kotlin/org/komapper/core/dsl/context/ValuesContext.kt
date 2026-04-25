package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.SelectOptions

@ThreadSafe
data class ValuesContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val rows: List<List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>>,
    override val options: SelectOptions,
) : SubqueryContext

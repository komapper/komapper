package org.komapper.core.dsl.expression

import org.komapper.core.dsl.context.ValuesContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class ValuesExpression<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: ValuesContext<ENTITY, ID, META>,
) : SubqueryExpression<ENTITY>

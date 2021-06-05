package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel

interface FlowQueryVisitor {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
            visit(query: SqlSelectQueryImpl.FlowQueryImpl<ENTITY, ID, META>): FlowQueryRunner

}
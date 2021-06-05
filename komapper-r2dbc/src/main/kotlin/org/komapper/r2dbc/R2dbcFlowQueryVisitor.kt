package org.komapper.r2dbc

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.FlowQueryRunner
import org.komapper.core.dsl.query.FlowQueryVisitor
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.r2dbc.dsl.runner.RowTransformers
import org.komapper.r2dbc.dsl.runner.SqlSelectFlowQueryRunner

internal class R2dbcFlowQueryVisitor : FlowQueryVisitor {
    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlSelectQueryImpl.FlowQueryImpl<ENTITY, ID, META>): FlowQueryRunner {
        val transform = RowTransformers.singleEntity(query.context.target)
        return SqlSelectFlowQueryRunner(query.context, query.option, transform)
    }
}

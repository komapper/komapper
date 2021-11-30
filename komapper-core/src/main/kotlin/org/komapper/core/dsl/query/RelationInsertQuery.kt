package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationInsertSelectContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface RelationInsertQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R> : Query<R> {
    fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, R>
    fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>>
}

internal data class RelationInsertSelectQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertSelectContext<ENTITY, ID, META>,
) : RelationInsertQuery<ENTITY, ID, META, Pair<Int, List<ID>>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, Pair<Int, List<ID>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>> {
        val newContext = context.asRelationInsertValuesContext(declaration)
        return RelationInsertValuesQuery(newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertSelectQuery(context)
    }
}

internal data class RelationInsertValuesQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun values(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertQuery<ENTITY, ID, META, Pair<Int, ID?>> {
        val newContext = context.copy(values = context.values + declaration)
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationInsertValuesQuery(context)
    }
}

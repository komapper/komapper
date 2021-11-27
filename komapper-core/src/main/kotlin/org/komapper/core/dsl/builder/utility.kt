package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.WhereProvider
import org.komapper.core.dsl.element.ColumnsAndSource
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.AssignmentScope
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.WhereScope

internal fun WhereProvider.getWhereCriteria(): List<Criterion> {
    val where = getCompositeWhere()
    return WhereScope().apply(where)
}

internal fun Join<*, *, *>.getOnCriteria(): List<Criterion> {
    return OnScope().apply(on)
}

internal fun SelectContext<*, *, *>.getHavingCriteria(): List<Criterion> {
    return HavingScope().apply(having)
}

internal fun <ENTITY : Any> RelationUpdateContext<ENTITY, *, *>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return AssignmentScope<ENTITY>().apply(set)
}

internal fun <ENTITY : Any> ColumnsAndSource.Values<ENTITY>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return AssignmentScope<ENTITY>().apply(declaration)
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    val context = this
    return AssignmentScope<ENTITY>().apply {
        context.set(this, context.excluded)
    }
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.createAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    fun properties(meta: META): List<PropertyMetamodel<ENTITY, *, *>> {
        return meta.properties().filter { it != meta.createdAtProperty() } - meta.idProperties().toSet()
    }
    return properties(target).zip(properties(excluded)).map { (p1, p2) ->
        p1 to Operand.Column(p2)
    }
}
